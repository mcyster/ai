package com.cyster.ai.weave.impl.advisor.assistant;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.cyster.ai.weave.service.conversation.Operation;

import io.github.stefanbratanov.jvm.openai.CreateMessageRequest;
import io.github.stefanbratanov.jvm.openai.CreateRunRequest;
import io.github.stefanbratanov.jvm.openai.CreateThreadRequest;
import io.github.stefanbratanov.jvm.openai.MessagesClient;
import io.github.stefanbratanov.jvm.openai.OpenAIException;
import io.github.stefanbratanov.jvm.openai.PaginationQueryParameters;
import io.github.stefanbratanov.jvm.openai.Role;
import io.github.stefanbratanov.jvm.openai.RunsClient;
import io.github.stefanbratanov.jvm.openai.SubmitToolOutputsRequest;
import io.github.stefanbratanov.jvm.openai.ThreadRun;
import io.github.stefanbratanov.jvm.openai.ThreadsClient;
import io.github.stefanbratanov.jvm.openai.ToolCall;
import io.github.stefanbratanov.jvm.openai.Thread;
import io.github.stefanbratanov.jvm.openai.ThreadMessage;
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
    private List<Message> newMessages;
    private Optional<Thread> thread = Optional.empty();
    private Optional<String> overrideInstructions = Optional.empty();
    private C context;

    // TODO make override instruction, system messages
    AssistantAdvisorConversation(OpenAiService openAiService, String assistantId, Toolset<C> toolset,
        Optional<String> overrideInstructions, C context) {
        this.openAiService = openAiService;
        this.assistantId = assistantId;
        this.toolset = toolset;
        this.messages = new ArrayList<Message>();
        this.newMessages = new ArrayList<Message>();
        this.overrideInstructions = overrideInstructions;
        this.context = context;
    }

    @Override
    public Message addMessage(Type type, String content) {
        var message = new MessageImpl(type, content);

        newMessages.add(message);

        return message;
    }

    @Override
    public Message respond() throws ConversationException {
        var operations = new OperationImpl("Assistant");

        var thread = getOrCreateThread(operations);

        int retries = 0;
        String response = null;
        do {
            try {
                for(var message: this.newMessages) {
                    addThreadedMessage(thread, message, operations);
                }
                newMessages.clear();

                response = doRun(thread, operations);
            } catch (RetryableAdvisorConversationException exception) {
                retries = retries + 1;
                if (retries > CONVERSATION_RETIES_MAX) {
                    throw new ConversationException("Advisor experienced problems responding to conversation, tried "
                        + retries + " times", exception);
                }
                logger.warn("Advisor thread run failed, retrying");
            } catch (AdvisorConversationException exception) {
                this.messages.add(new MessageImpl(Type.ERROR, exception.getMessage(), operations));
                throw new ConversationException("Advisor experienced problems responding to conversation", exception);
            }
        } while (response == null);

        // TODO add message at the start so we can follow the operations live
        var responseMessage = new MessageImpl(Type.AI, response, operations);
        this.messages.add(responseMessage);

        return responseMessage;
    }

    @Override
    public List<Message> getMessages() {
        return Stream.concat(this.messages.stream(), this.newMessages.stream())
            .collect(Collectors.toList());
    }

    private String doRun(Thread thread, OperationLogger operations) throws AdvisorConversationException {
        RunsClient runsClient = this.openAiService.createClient(RunsClient.class, operations);

        int retryCount = 0;
        long delay = RUN_BACKOFF_MIN;
        long attempts = 0;

        var requestBuilder = CreateRunRequest.newBuilder()
            .assistantId(this.assistantId);
        ThreadRun run = runsClient.createRun(thread.id(), requestBuilder.build());

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

            if (run.status().equals("expired")) {
                throw new RetryableAdvisorConversationException("Run.expired");
            }

            if (run.status().equals("failed")) {
                System.out.println("!!!Run Failed: " + run);
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
                    operations.log(Operation.Level.Normal, "Toolcall: " + toolCall.toString(), toolOutput);
                }

                try {
                    runsClient.submitToolOutputs(run.threadId(), run.id(), toolOutputsBuilder.build());
                } catch(OpenAIException exception) {
                    throw new AdvisorConversationException("Submitting tool run failed", exception);
                }
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

            logger.info("Run.status[" + run.id() + "]: " + run.status() + " (delay " + delay + "ms)");
        } while (!run.status().equals("completed"));

        MessagesClient messagesClient = openAiService.createClient(MessagesClient.class, operations);

        var responseMessages = messagesClient.listMessages(thread.id(), PaginationQueryParameters.none(), Optional.empty());

        if (responseMessages.data().size() == 0) {
            operations.log(Operation.Level.Normal, "No Response from AI", null);
            throw new AdvisorConversationException("No Reponses");
        }
        var responseMessage = responseMessages.data().get(0);
        if (!responseMessage.role().equals("assistant")) {
            operations.log(Operation.Level.Normal, "Assistant did not respond", null);
            throw new AdvisorConversationException("Assistant did not respond");
        }

        var content = responseMessage.content();
        if (content.size() == 0) {
            operations.log(Operation.Level.Normal, "Assistant responded with no content", null);
            throw new AdvisorConversationException("No Content: " + responseMessages.toString());
        }

        if (content.size() > 1) {
            operations.log(Operation.Level.Normal, "Assistant responded with lots of content, ignoring", null);
            throw new AdvisorConversationException("Lots of Content");
        }

        if (!content.get(0).type().equals("text")) {
            operations.log(Operation.Level.Normal, "Assistant responded with non text content, ignoring", null);
            throw new AdvisorConversationException("Content not of type text");
        }
        var textContent = (TextContent)content.get(0);

        return textContent.text().value();
    }

    private ThreadMessage addThreadedMessage(Thread thread, Message message, OperationLogger operations) {
        MessagesClient messagesClient = openAiService.createClient(MessagesClient.class, operations);

        Role role;
        switch(message.getType()) {
        case AI:
           role = Role.ASSISTANT;
           break;
        case SYSTEM:
            role = Role.ASSISTANT;
            break;
        case USER:
            role = Role.USER;
            break;
        default:
            throw new RuntimeException("Unexpected message type");
        }

        var createMessageRequest = CreateMessageRequest.newBuilder()
            .role(role)
            .content(message.getContent())
            .build();

         return messagesClient.createMessage(thread.id(), createMessageRequest);
    }

    private Thread getOrCreateThread(OperationLogger operations) {
        ThreadsClient threadsClient = openAiService.createClient(ThreadsClient.class, operations);

        if (thread.isEmpty()) {
            var threadRequestBuilder = CreateThreadRequest.newBuilder();

            if (overrideInstructions.isPresent()) {
                var threadMessage = CreateThreadRequest.Message.newBuilder()
                    .role(Role.ASSISTANT)
                    .content(overrideInstructions.get())
                    .build();
                threadRequestBuilder.message(threadMessage);
            }

            for (var message : this.newMessages) {
                if (message.getType() == Type.AI) {
                    var threadMessage = CreateThreadRequest.Message.newBuilder()
                        .role(Role.ASSISTANT)
                        .content(message.getContent())
                        .build();

                    threadRequestBuilder.message(threadMessage);
                }
                else if (message.getType() == Type.SYSTEM) {
                    var threadMessage = CreateThreadRequest.Message.newBuilder()
                        .role(Role.ASSISTANT)
                        .content(message.getContent())
                        .build();

                        threadRequestBuilder.message(threadMessage);
                }
                else if (message.getType() == Type.USER) {
                    var threadMessage = CreateThreadRequest.Message.newBuilder()
                        .role(Role.USER)
                        .content(message.getContent())
                        .build();

                        threadRequestBuilder.message(threadMessage);
                }
            }
            this.newMessages.clear();

            this.thread = Optional.of(threadsClient.createThread(threadRequestBuilder.build()));
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
