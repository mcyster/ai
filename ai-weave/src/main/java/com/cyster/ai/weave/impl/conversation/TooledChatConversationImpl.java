package com.cyster.ai.weave.impl.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.cyster.ai.weave.impl.advisor.ChatFunctionToolset;
import com.cyster.ai.weave.impl.advisor.MessageImpl;
import com.cyster.ai.weave.impl.advisor.Toolset;
import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.advisor.Tool;
import com.cyster.ai.weave.service.advisor.TooledChatConversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;

import io.github.stefanbratanov.jvm.openai.ChatClient;
import io.github.stefanbratanov.jvm.openai.ChatMessage;
import io.github.stefanbratanov.jvm.openai.CreateChatCompletionRequest;
import io.github.stefanbratanov.jvm.openai.ChatMessage.ToolMessage;
import io.github.stefanbratanov.jvm.openai.ToolCall.FunctionToolCall;

// TODO TooledChatAdvisor is the generic form of this - remove one impl
public class TooledChatConversationImpl implements TooledChatConversation {
    private final String MODEL = "gpt-4o";

    private OpenAiService openAiService;
    private List<Message> messages;
    private Toolset.Builder<Void> toolsetBuilder;

    public TooledChatConversationImpl(OpenAiService openAiService) {
        this.openAiService = openAiService;
        this.messages = new ArrayList<Message>();
        this.toolsetBuilder = new Toolset.Builder<Void>();
    }

    public Message addMessage(Type type, String content) {
        Message message = new MessageImpl(type, content);
        this.messages.add(message);

        return message;
    }


    public TooledChatConversationImpl addUserMessage(String content) {
        this.messages.add(new MessageImpl(content));
        return this;
    }
    
    public TooledChatConversationImpl addSystemMessage(String content) {
        this.messages.add(new MessageImpl(Message.Type.SYSTEM, content));
        return this;
    }
    
    public TooledChatConversationImpl addAiMessage(String content) {
        this.messages.add(new MessageImpl(Message.Type.AI, content));
        return this;
    }
    public <T> TooledChatConversationImpl addTool(String name, String description, Class<T> parameterClass,
        Function<T, Object> executor) {
        var tool = new ChatToolPojo<T>(name, description, parameterClass, executor);
        return this.addTool(tool);
    }

    public <T> TooledChatConversationImpl addTool(Tool<T, Void> tool) {
        this.toolsetBuilder.addTool(tool);
        return this;
    }

    @Override
    public Message respond() {
        ChatClient chatClient = openAiService.createClient(ChatClient.class);
        Message response = null;

        while (response == null) {
            var chatMessages = new ArrayList<ChatMessage>();

            for (var message : this.messages) {
                switch (message.getType()) {
                case SYSTEM:
                    chatMessages.add(ChatMessage.systemMessage(message.getContent()));
                    break;
                case AI:
                    chatMessages.add(ChatMessage.assistantMessage(message.getContent()));
                    break;
                case USER:
                    chatMessages.add(ChatMessage.userMessage(message.getContent()));
                    break;
                default:
                    // ignore
                }
            }

            Toolset<Void> toolset = this.toolsetBuilder.create();
                        
            var chatCompletionRequest = CreateChatCompletionRequest.newBuilder()
                .model(MODEL)
                .messages(chatMessages)
                .maxTokens(1000)
                .build();

            var chatResponse = chatClient.createChatCompletion(chatCompletionRequest);

            var choices = chatResponse.choices();
            if (choices.size() > 1) {
                messages.add(new MessageImpl(Message.Type.INFO, "Multiple responses (ignored, only taking 1st response)"));
            }
            var choice = choices.get(0);

            switch (choice.finishReason()) {
            case "stop":
                var messageContent = choice.message().content();
                response = new MessageImpl(Message.Type.AI, messageContent);
                messages.add(response);
                break;

            case "length":
                messages.add(new MessageImpl(Message.Type.ERROR, "Token Limit Exceeded"));
                break;

            case "content_filter":
                messages.add(new MessageImpl(Message.Type.ERROR, "Content Filtered"));
                break;

            case "function_call":
                var chatFunctionToolset = new ChatFunctionToolset<Void>(toolset);  

                for(var toolCall: choice.message().toolCalls()) {
                    if (toolCall.type() != "function") {
                        messages.add(new MessageImpl(Message.Type.ERROR, "Tool call not a function"));
                        continue;
                    }
                    FunctionToolCall functionToolCall = (FunctionToolCall)toolCall;
                    
                    messages.add(new MessageImpl(Message.Type.FUNCTION_CALL, functionToolCall.function().name() 
                        + "(" + functionToolCall.function().arguments() + ")"));
    
                   ToolMessage toolMessage = chatFunctionToolset.call(functionToolCall);
                   messages.add(new MessageImpl(Message.Type.FUNCTION_RESULT, toolMessage.content()));
                }
                break;

            default:
                messages.add(new MessageImpl(Message.Type.ERROR, "Unexpected finish reason: " + choice.finishReason()));
            }
        }

        return response;
    }

    private static class ChatToolPojo<T> implements Tool<T, Void> {
        private String name;
        private String description;
        private Class<T> parameterClass;
        private Function<T, Object> executor;

        public ChatToolPojo(String name, String description, Class<T> parameterClass, Function<T, Object> executor) {
            this.name = name;
            this.description = description;
            this.parameterClass = parameterClass;
            this.executor = executor;
        }
        
        public String getName() {
            return this.name;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public Class<T> getParameterClass() {
            return this.parameterClass;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object execute(Object parameters, Void context) {
            return this.executor.apply((T)parameters);   
        }

    }

    @Override
    public List<Message> getMessages() {
        return messages.stream()
            // .filter(message -> message.getType() == Message.Type.AI || message.getType()
            // == Message.Type.USER)
            .collect(Collectors.toList());
    }

}