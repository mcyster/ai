package com.cyster.ai.weave.impl.advisor;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;

import io.github.stefanbratanov.jvm.openai.CreateMessageRequest;
import io.github.stefanbratanov.jvm.openai.CreateRunRequest;
import io.github.stefanbratanov.jvm.openai.CreateThreadRequest;
import io.github.stefanbratanov.jvm.openai.MessagesClient;
import io.github.stefanbratanov.jvm.openai.OpenAIException;
import io.github.stefanbratanov.jvm.openai.PaginationQueryParameters;
import io.github.stefanbratanov.jvm.openai.RunsClient;
import io.github.stefanbratanov.jvm.openai.SubmitToolOutputsRequest;
import io.github.stefanbratanov.jvm.openai.ThreadRun;
import io.github.stefanbratanov.jvm.openai.ThreadsClient;
import io.github.stefanbratanov.jvm.openai.ToolCall;
import io.github.stefanbratanov.jvm.openai.Thread;
import io.github.stefanbratanov.jvm.openai.ThreadMessage.Content.TextContent;
import io.github.stefanbratanov.jvm.openai.SubmitToolOutputsRequest.ToolOutput;
import io.github.stefanbratanov.jvm.openai.ToolCall.FunctionToolCall;

public class AssistantAdvisorConversation<C> implements Conversation {
    private static final long RUN_BACKOFF_MIN = 1000L;
    private static final long RUN_BACKOFF_MAX = 1000 * 60 * 1L;
    private static final long RUN_POLL_ATTEMPTS_MAX = 100;
    private static final long RUN_RETRIES_MAX = 5;
    private static final int MAX_PARAMETER_LENGTH = 50;
    private static final String ELIPSES = "...";
    private static final int CONVERSATION_RETIES_MAX = 3;

    private static final Logger logger = LoggerFactory.getLogger(AssistantAdvisorConversation.class);

    private OpenAiService openAiService;
    private String assistantId;
    private Toolset<C> toolset;
    private List<Message> messages;
    private Optional<Thread> thread = Optional.empty();
    private Optional<String> overrideInstructions = Optional.empty();
    private C context;

    AssistantAdvisorConversation(OpenAiService openAiService, String assistantId, Toolset<C> toolset,
        Optional<String> overrideInstructions, C context) {
        this.openAiService = openAiService;
        this.assistantId = assistantId;
        this.toolset = toolset;
        this.messages = new ArrayList<Message>();
        this.overrideInstructions = overrideInstructions;
        this.context = context;
    }

    @Override
    public Conversation addMessage(String message) {
        var typedMessage = new MessageImpl(message);

        this.messages.add(typedMessage);

        return this;
    }

    @Override
    public Message respond() throws ConversationException {
        var operations = new OperationImpl("Assistant");
 
        int retries = 0;

        String response = null;
        do {
            try {
                response = doRun(operations);
            } catch (RetryableAdvisorConversationException exception) {
                retries = retries + 1;
                if (retries > CONVERSATION_RETIES_MAX) {
                    throw new ConversationException("Advisor experienced problems responding to conversation, tried "
                        + retries + " times", exception);
                }
                logger.warn("Advisor thread run failed, retrying");
            } catch (AdvisorConversationException exception) {
                operations.log("Conversation Failed", exception.getMessage());
                throw new ConversationException("Advisor experienced problems responding to conversation", exception);
            }
        } while (response == null);

 
        var message = new MessageImpl(Type.AI, response, operations);  // TODO add the message at the start, so we can follow
        this.messages.add(message);
        
        return message;
    }

    @Override
    public List<Message> getMessages() {
        return this.messages;
    }

    private String doRun(OperationLogger operations) throws AdvisorConversationException {
        var thread = getOrCreateThread(operations);

        var requestBuilder = CreateRunRequest.newBuilder()
            .assistantId(this.assistantId);

        if (overrideInstructions.isPresent()) {
            requestBuilder.instructions(overrideInstructions.get());
        }

        RunsClient runsClient = this.openAiService.createClient(RunsClient.class, operations);
        ThreadRun run = runsClient.createRun(thread.id(), requestBuilder.build());

        int retryCount = 0;
        long delay = RUN_BACKOFF_MIN;
        long attempts = 0;
        String lastStatus = "";

        do {
            try {
                if (lastStatus.equals(run.status())) {
                    java.lang.Thread.sleep(delay);
                    delay *= 2;
                    if (delay > RUN_BACKOFF_MAX) {
                        delay = RUN_BACKOFF_MAX;
                    }
                } else {
                    delay /= 2;
                    if (delay < RUN_BACKOFF_MIN) {
                        delay = RUN_BACKOFF_MIN;
                    }
                }
                lastStatus = run.status();
            } catch (InterruptedException exception) {
                throw new RuntimeException("Thread interrupted with waitfinr for OpenAI run response", exception);
            }

            if (attempts > RUN_POLL_ATTEMPTS_MAX) {
                throw new AdvisorConversationException("Exceeded maximum openai thread run retry attempts ("
                    + RUN_POLL_ATTEMPTS_MAX
                    + ") while waiting for a response for an openai run");
            }

            try {
                run = runsClient.retrieveRun(run.threadId(), run.id());
            } catch (Throwable exception) {
                if (exception instanceof SocketTimeoutException) {
                    if (retryCount++ > RUN_RETRIES_MAX) {
                        throw new AdvisorConversationException("Socket Timeout while checking OpenAi.run.status",
                            exception);
                    }
                } else {
                    throw new AdvisorConversationException("Error while checking OpenAi.run.status", exception);
                }
            }

            if (run.status().equals("expired")) {
                throw new RetryableAdvisorConversationException("Run.expired");
            }

            if (run.status().equals("failed")) {
                throw new AdvisorConversationException("Run.failed");
            }

            if (run.status().equals("cancelled")) {
                throw new AdvisorConversationException("Run.cancelled");
            }

            if (run.requiredAction() != null) {
                logger.info("Run.actions[" + run.id() + "]: " + run.requiredAction().submitToolOutputs()
                    .toolCalls().stream()
                        .map(toolCall -> getToolCallSummary(toolCall))
                        .collect(Collectors.joining(", ")));

                if (run.requiredAction().submitToolOutputs() == null
                    || run.requiredAction().submitToolOutputs() == null
                    || run.requiredAction().submitToolOutputs().toolCalls() == null) {
                    throw new AdvisorConversationException("Action Required but no details");
                }
 
                SubmitToolOutputsRequest.Builder toolOutputsBuilder = SubmitToolOutputsRequest.newBuilder();
                
                for (var toolCall : run.requiredAction().submitToolOutputs().toolCalls()) {
                    if (!toolCall.type().equals("function")) {
                        throw new AdvisorConversationException("Unexpected tool call - not a function");
                    }
                    FunctionToolCall functionToolCall = (FunctionToolCall)toolCall;
                    
                    var callId = functionToolCall.id();

                    var output = this.toolset.execute(functionToolCall.function().name(), 
                        functionToolCall.function().arguments(), this.context);
                    
                    ToolOutput toolOutput = ToolOutput.newBuilder().toolCallId(callId).output(output).build();
                    
                    toolOutputsBuilder.toolOutput(toolOutput);
                    messages.add(new MessageImpl(Message.Type.INFO, "Toolcall: " + toolCall.toString() + " Response: "
                        + toolOutput.toString()));                    
                }
                   
                try {
                    runsClient.submitToolOutputs(run.threadId(), run.id(), toolOutputsBuilder.build());
                } catch(OpenAIException exception) {
                    throw new AdvisorConversationException("Submitting tool run failed", exception);
                }
            }

            
            logger.info("Run.status[" + run.id() + "]: " + run.status() + " (delay " + delay + "ms)");
        } while (!run.status().equals("completed"));

        MessagesClient messagesClient = openAiService.createClient(MessagesClient.class, operations);
        
        var responseMessages = messagesClient.listMessages(thread.id(), PaginationQueryParameters.none(), Optional.empty());

        if (responseMessages.data().size() == 0) {
            messages.add(new MessageImpl(Message.Type.INFO, "No responses"));
            throw new AdvisorConversationException("No Reponses");
        }
        var responseMessage = responseMessages.data().get(0);
        if (!responseMessage.role().equals("assistant")) {
            messages.add(new MessageImpl(Message.Type.INFO, "Assistant did not response"));
            throw new AdvisorConversationException("Assistant did not respond");
        }

        var content = responseMessage.content();
        if (content.size() == 0) {
            throw new AdvisorConversationException("No Content");
        }

        if (content.size() > 1) {
            throw new AdvisorConversationException("Lots of Content");
        }

        if (!content.get(0).type().equals("text")) {
            throw new AdvisorConversationException("Content not of type text");
        }
        var textContent = (TextContent)content.get(0);
        
        return textContent.text().value();
    }

    private Thread getOrCreateThread(OperationLogger operations) {
        ThreadsClient threadsClient = openAiService.createClient(ThreadsClient.class, operations);
        MessagesClient messagesClient = openAiService.createClient(MessagesClient.class, operations);

        if (thread.isEmpty()) {
            var threadRequest = CreateThreadRequest.newBuilder().build();

            this.thread = Optional.of(threadsClient.createThread(threadRequest));

            for (var message : this.messages) {
                if (message.getType() == Type.USER) {
                    CreateMessageRequest messageRequest = CreateMessageRequest.newBuilder()
                        .role("user")
                        .content(message.getContent())
                        .build();
                    messagesClient.createMessage(this.thread.get().id(), messageRequest);
                }
            }
        }

        return this.thread.get();
    }

    private static String getToolCallSummary(ToolCall toolCall) {
        if (toolCall.type() == "function") {
            FunctionToolCall functionToolCall = (FunctionToolCall)toolCall;
            String name = functionToolCall.function().name();
            
            String arguments = escapeNonAlphanumericCharacters(functionToolCall.function().arguments());
            
            if (arguments.length() > MAX_PARAMETER_LENGTH) {
                arguments = arguments.substring(0, MAX_PARAMETER_LENGTH - ELIPSES.length()) + ELIPSES;
            }
            
            return name + "(" + arguments + ")";
        } else {                        
            return toolCall.toString();
        }
    }
    
    public static String escapeNonAlphanumericCharacters(String input) {
        StringBuilder result = new StringBuilder();
        for (char character : input.toCharArray()) {
            if (isPrintable(character)) {
                result.append(character);
            } else {
                result.append(escapeCharacter(character));
            }
        }
        return result.toString();
    }
    
    private static boolean isPrintable(char character) {
        return character >= 32 && character <= 126;  
    }
    
    public static String escapeCharacter(char character) {
        switch (character) {
            case '\n':
                return "\\n";
            case '\t':
                return "\\t";
            default:
                return "\\" + character;
        }
    }
}
