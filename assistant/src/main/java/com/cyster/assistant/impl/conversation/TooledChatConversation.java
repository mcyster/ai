package com.cyster.assistant.impl.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.cyster.assistant.impl.advisor.ChatFunctionToolset;
import com.cyster.assistant.impl.advisor.Tool;
import com.cyster.assistant.impl.advisor.Toolset;
import com.cyster.assistant.service.conversation.Conversation;
import com.cyster.assistant.service.conversation.Message;

import io.github.stefanbratanov.jvm.openai.ChatClient;
import io.github.stefanbratanov.jvm.openai.ChatMessage;
import io.github.stefanbratanov.jvm.openai.CreateChatCompletionRequest;
import io.github.stefanbratanov.jvm.openai.OpenAI;
import io.github.stefanbratanov.jvm.openai.ChatMessage.ToolMessage;
import io.github.stefanbratanov.jvm.openai.ToolCall.FunctionToolCall;

// TODO TooledChatAdvisor is the generic form of this - remove one impl
public class TooledChatConversation implements Conversation {

    private final String model = "gpt-3.5-turbo-0613";

    private OpenAI openAi;
    private List<Message> messages;
    private Toolset.Builder<Void> toolsetBuilder;

    public TooledChatConversation(OpenAI openAi) {
        this.openAi = openAi;
        this.messages = new ArrayList<Message>();
        this.toolsetBuilder = new Toolset.Builder<Void>();
    }

    @Override
    public TooledChatConversation addMessage(String content) {
        this.messages.add(new Message(content));

        return this;
    }


    public TooledChatConversation addUserMessage(String content) {
        this.messages.add(new Message(content));
        return this;
    }
    
    public TooledChatConversation addSystemMessage(String content) {
        this.messages.add(new Message(Message.Type.SYSTEM, content));
        return this;
    }
    
    public TooledChatConversation addAiMessage(String content) {
        this.messages.add(new Message(Message.Type.AI, content));
        return this;
    }
    public <T> TooledChatConversation addTool(String name, String description, Class<T> parameterClass,
        Function<T, Object> executor) {
        var tool = new ChatToolPojo<T>(name, description, parameterClass, executor);
        return this.addTool(tool);
    }

    public <T> TooledChatConversation addTool(Tool<T, Void> tool) {
        this.toolsetBuilder.addTool(tool);
        return this;
    }

    @Override
    public Message respond() {
        ChatClient chatClient = openAi.chatClient();
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
                    /*
                case FUNCTION_CALL:
                    chatMessages.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), message.getContent(),
                        "get_weather"));
                case FUNCTION_RESULT:
                    chatMessages.add(new ChatMessage(ChatMessageRole.FUNCTION.value(), message.getContent(),
                        "get_weather"));
                    break;
                    */
                default:
                    // ignore
                }
            }

            Toolset<Void> toolset = this.toolsetBuilder.create();
                        
            var chatCompletionRequest = CreateChatCompletionRequest.newBuilder()
                .model(model)
                .messages(chatMessages)
//                .functions(chatFunctionToolset.getFunctions())
//                .functionCall(new ChatCompletionRequestFunctionCall("auto"))
                .maxTokens(1000)
                .build();

            var chatResponse = chatClient.createChatCompletion(chatCompletionRequest);

            var choices = chatResponse.choices();
            if (choices.size() > 1) {
                messages.add(new Message(Message.Type.INFO, "Multiple responses (ignored, only taking 1st response)"));
            }
            var choice = choices.get(0);

            switch (choice.finishReason()) {
            case "stop":
                var messageContent = choice.message().content();
                response = new Message(Message.Type.AI, messageContent);
                messages.add(response);
                break;

            case "length":
                messages.add(new Message(Message.Type.ERROR, "Token Limit Exceeded"));
                break;

            case "content_filter":
                messages.add(new Message(Message.Type.ERROR, "Content Filtered"));
                break;

            case "function_call":
                var chatFunctionToolset = new ChatFunctionToolset<Void>(toolset);  

                for(var toolCall: choice.message().toolCalls()) {
                    if (toolCall.type() != "function") {
                        messages.add(new Message(Message.Type.ERROR, "Tool call not a function"));
                        continue;
                    }
                    FunctionToolCall functionToolCall = (FunctionToolCall)toolCall;
                    
                    messages.add(new Message(Message.Type.FUNCTION_CALL, functionToolCall.function().name() 
                        + "(" + functionToolCall.function().arguments() + ")"));
    
                   ToolMessage toolMessage = chatFunctionToolset.call(functionToolCall);
                   messages.add(new Message(Message.Type.FUNCTION_RESULT, toolMessage.content()));
                }
                break;

            default:
                messages.add(new Message(Message.Type.ERROR, "Unexpected finish reason: " + choice.finishReason()));
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