package com.cyster.ai.weave.impl.openai.advisor.assistant;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyster.ai.weave.impl.MessageImpl;
import com.cyster.ai.weave.impl.WeaveImpl;
import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.impl.tool.Toolset;
import com.cyster.ai.weave.service.Weave;
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
import io.github.stefanbratanov.jvm.openai.SubmitToolOutputsRequest.ToolOutput;
import io.github.stefanbratanov.jvm.openai.Thread;
import io.github.stefanbratanov.jvm.openai.ThreadMessage;
import io.github.stefanbratanov.jvm.openai.ThreadMessage.Content.TextContent;
import io.github.stefanbratanov.jvm.openai.ThreadRun;
import io.github.stefanbratanov.jvm.openai.ThreadsClient;
import io.github.stefanbratanov.jvm.openai.ToolCall;
import io.github.stefanbratanov.jvm.openai.ToolCall.FunctionToolCall;

public class AssistantAdvisorThread<CONTEXT> {
    private static final long RUN_BACKOFF_MIN = 1000L;
    private static final long RUN_BACKOFF_MAX = 1000 * 60 * 1L;
    private static final long RUN_POLL_ATTEMPTS_MAX = 100;
    private static final long RUN_RETRIES_MAX = 5;
    private static final int MAX_PARAMETER_LENGTH = 50;
    private static final String ELIPSES = "...";
    private static final int CONVERSATION_RETIES_MAX = 3;

    private static final Logger logger = LoggerFactory.getLogger(AssistantAdvisorConversation.class);

    private final OpenAiService openAiService;
    private final String assistantName;
    private final String assistantId;
    private final Toolset<CONTEXT> toolset;
    private final Optional<String> overrideInstructions;
    private final CONTEXT context;
    private Optional<Thread> thread = Optional.empty();

    AssistantAdvisorThread(OpenAiService openAiService, String assistantName, String assistantId,
            Toolset<CONTEXT> toolset, Optional<String> overrideInstructions, CONTEXT context) {
        this.openAiService = openAiService;
        this.assistantName = assistantName;
        this.assistantId = assistantId;
        this.toolset = toolset;
        this.overrideInstructions = overrideInstructions;
        this.context = context;
    }

    public Message respond(List<Message> messages, Weave weave) throws ConversationException {
        if (thread.isEmpty()) {
            thread = Optional.of(createThread(messages, weave));
        } else {
            for (var message : messages) {
                addThreadedMessage(thread.get(), message, weave);
            }
        }

        int retries = 0;
        String response = null;
        do {
            try {
                response = doRun(thread.get(), weave);
            } catch (RetryableAssistantAdvisorConversationException exception) {
                retries = retries + 1;
                if (retries > CONVERSATION_RETIES_MAX) {
                    throw new ConversationException(
                            "Advisor experienced problems responding to conversation, tried " + retries + " times",
                            exception);
                }
                logger.warn("Advisor thread run failed, retrying");
            } catch (AssistantAdvisorConversationException exception) {
                throw new ConversationException("Advisor experienced problems responding to conversation", exception);
            }
        } while (response == null);

        return new MessageImpl(Type.AI, response, weave.operation());
    }

    private String doRun(Thread thread, Weave weave) throws AssistantAdvisorConversationException {
        RunsClient runsClient = this.openAiService.createClient(RunsClient.class, weave,
                Map.of("assistantId", this.assistantId, "threadId", thread.id()));

        int retryCount = 0;
        long delay = RUN_BACKOFF_MIN;
        long attempts = 0;

        var requestBuilder = CreateRunRequest.newBuilder().assistantId(this.assistantId);

        Optional<List<String>> include = Optional.empty();
        ThreadRun run = runsClient.createRun(thread.id(), include, requestBuilder.build());

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
                throw new RuntimeException("Thread interrupted with waitfing for OpenAI run response", exception);
            }

            if (attempts > RUN_POLL_ATTEMPTS_MAX) {
                throw new AssistantAdvisorConversationException("Exceeded maximum openai thread run retry attempts ("
                        + RUN_POLL_ATTEMPTS_MAX + ") while waiting for a response for an openai run");
            }

            if (run.status().equals("expired")) {
                throw new RetryableAssistantAdvisorConversationException("Run.expired, Run: " + run.toString());
            }

            if (run.status().equals("failed")) {
                throw new AssistantAdvisorConversationException("Run.failed! Run: " + run.toString());
            }

            if (run.status().equals("cancelled")) {
                throw new AssistantAdvisorConversationException("Run.cancelled. Run: " + run.toString());
            }

            if (run.requiredAction() != null) {
                logger.info("Run.actions[" + run.id() + "]: " + run.requiredAction().submitToolOutputs().toolCalls()
                        .stream().map(toolCall -> getToolCallSummary(toolCall)).collect(Collectors.joining(", ")));

                if (run.requiredAction().submitToolOutputs() == null || run.requiredAction().submitToolOutputs() == null
                        || run.requiredAction().submitToolOutputs().toolCalls() == null) {
                    throw new AssistantAdvisorConversationException("Action Required but no details");
                }

                SubmitToolOutputsRequest.Builder toolOutputsBuilder = SubmitToolOutputsRequest.newBuilder();

                for (var toolCall : run.requiredAction().submitToolOutputs().toolCalls()) {
                    if (!toolCall.type().equals("function")) {
                        throw new AssistantAdvisorConversationException("Unexpected tool call - not a function");
                    }
                    FunctionToolCall functionToolCall = (FunctionToolCall) toolCall;

                    var callId = functionToolCall.id();

                    logger.info("Assistant " + this.assistantName + " assistantId " + this.assistantId + " running: "
                            + functionToolCall.function().name() + " with parameters "
                            + functionToolCall.function().arguments() + " and context " + this.context);

                    var output = this.toolset.execute(functionToolCall.function().name(),
                            functionToolCall.function().arguments(), this.context,
                            new WeaveImpl(weave.conversation(),
                                    weave.operation().childLogger("Tool - " + functionToolCall.function().name(),
                                            functionToolCall.function().arguments())));

                    ToolOutput toolOutput = ToolOutput.newBuilder().toolCallId(callId).output(output).build();

                    toolOutputsBuilder.toolOutput(toolOutput);
                    weave.operation().log(Operation.Level.Normal, "Toolcall: " + toolCall.toString(), toolOutput);
                }

                try {
                    runsClient.submitToolOutputs(run.threadId(), run.id(), toolOutputsBuilder.build());
                } catch (OpenAIException exception) {
                    throw new AssistantAdvisorConversationException("Submitting tool run failed", exception);
                }
            }

            try {
                run = runsClient.retrieveRun(run.threadId(), run.id());
            } catch (Throwable exception) {
                if (exception instanceof SocketTimeoutException) {
                    if (retryCount++ > RUN_RETRIES_MAX) {
                        throw new AssistantAdvisorConversationException(
                                "Socket Timeout while checking OpenAi.run.status", exception);
                    }
                } else {
                    throw new AssistantAdvisorConversationException("Error while checking OpenAi.run.status",
                            exception);
                }
            }

            logger.info("Run.status[" + run.id() + "]: " + run.status() + " (delay " + delay + "ms)");
        } while (!run.status().equals("completed"));

        MessagesClient messagesClient = openAiService.createClient(MessagesClient.class, weave,
                Map.of("threadId", thread.id()));

        var responseMessages = messagesClient.listMessages(thread.id(), PaginationQueryParameters.none(),
                Optional.empty());

        if (responseMessages.data().size() == 0) {
            weave.operation().log(Operation.Level.Normal, "No Response from AI", null);
            throw new AssistantAdvisorConversationException("No Reponses");
        }
        var responseMessage = responseMessages.data().get(0);
        if (!responseMessage.role().equals("assistant")) {
            weave.operation().log(Operation.Level.Normal, "Assistant did not respond", null);
            throw new AssistantAdvisorConversationException("Assistant did not respond");
        }

        var content = responseMessage.content();
        if (content.size() == 0) {
            weave.operation().log(Operation.Level.Normal, "Assistant responded with no content", null);
            throw new AssistantAdvisorConversationException("No Content: " + responseMessages.toString());
        }

        if (content.size() > 1) {
            weave.operation().log(Operation.Level.Normal, "Assistant responded with lots of content, ignoring", null);
            throw new AssistantAdvisorConversationException("Lots of Content");
        }

        if (!content.get(0).type().equals("text")) {
            weave.operation().log(Operation.Level.Normal, "Assistant responded with non text content, ignoring", null);
            throw new AssistantAdvisorConversationException("Content not of type text");
        }
        var textContent = (TextContent) content.get(0);

        return textContent.text().value();
    }

    private ThreadMessage addThreadedMessage(Thread thread, Message message, Weave weave) {
        MessagesClient messagesClient = openAiService.createClient(MessagesClient.class, weave,
                Map.of("threadId", thread.id()));

        Role role;
        switch (message.getType()) {
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

        var createMessageRequest = CreateMessageRequest.newBuilder().role(role).content(message.getContent()).build();

        return messagesClient.createMessage(thread.id(), createMessageRequest);
    }

    private Thread createThread(List<Message> messages, Weave weave) {
        ThreadsClient threadsClient = openAiService.createClient(ThreadsClient.class, weave, "createThread");

        var threadRequestBuilder = CreateThreadRequest.newBuilder();

        if (overrideInstructions.isPresent()) {
            var threadMessage = CreateThreadRequest.Message.newBuilder().role(Role.ASSISTANT)
                    .content(overrideInstructions.get()).build();
            threadRequestBuilder.message(threadMessage);
        }

        for (var message : messages) {
            if (message.getType() == Type.AI) {
                var threadMessage = CreateThreadRequest.Message.newBuilder().role(Role.ASSISTANT)
                        .content(message.getContent()).build();

                threadRequestBuilder.message(threadMessage);
            } else if (message.getType() == Type.SYSTEM) {
                var threadMessage = CreateThreadRequest.Message.newBuilder().role(Role.ASSISTANT)
                        .content(message.getContent()).build();

                threadRequestBuilder.message(threadMessage);
            } else if (message.getType() == Type.USER) {
                var threadMessage = CreateThreadRequest.Message.newBuilder().role(Role.USER)
                        .content(message.getContent()).build();

                threadRequestBuilder.message(threadMessage);
            }
        }

        var thread = threadsClient.createThread(threadRequestBuilder.build());

        return thread;
    }

    private static String getToolCallSummary(ToolCall toolCall) {
        if (toolCall.type() == "function") {
            FunctionToolCall functionToolCall = (FunctionToolCall) toolCall;
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
