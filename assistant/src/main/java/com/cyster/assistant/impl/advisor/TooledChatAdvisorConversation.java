package com.cyster.assistant.impl.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.cyster.assistant.service.conversation.Conversation;
import com.cyster.assistant.service.conversation.ConversationException;
import com.cyster.assistant.service.conversation.Message;

import io.github.stefanbratanov.jvm.openai.ChatClient;
import io.github.stefanbratanov.jvm.openai.ChatMessage;
import io.github.stefanbratanov.jvm.openai.ChatMessage.ToolMessage;
import io.github.stefanbratanov.jvm.openai.CreateChatCompletionRequest;
import io.github.stefanbratanov.jvm.openai.OpenAI;
import io.github.stefanbratanov.jvm.openai.ToolCall.FunctionToolCall;

public class TooledChatAdvisorConversation<C> implements Conversation {
    private final String MODEL = "gpt-4o";

    private OpenAI openAi;
    private Toolset<C> toolset;
    private List<Message> messages;
    
    TooledChatAdvisorConversation(OpenAI openAi, Toolset<C> toolset, List<Message> messages) {
        this.openAi = openAi;   
        this.toolset = toolset;
        this.messages = messages;
    }
    
    @Override
    public Conversation addMessage(String message) {
        this.messages.add(new Message(message));
        return this;
    }

    @Override
    public Message respond() throws ConversationException {
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
                default:
                    // ignore
                }
            }
                    
            var chatCompletionRequest = CreateChatCompletionRequest.newBuilder()
                .model(MODEL)
                .messages(chatMessages)
                .maxTokens(1000);
    
            var chatResponse = chatClient.createChatCompletion(chatCompletionRequest.build());
    
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
                var chatFunctionToolset = new ChatFunctionToolset<C>(this.toolset);
                
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

    @Override
    public List<Message> getMessages() {
        return messages.stream()
            .filter(message -> message.getType() == Message.Type.AI || message.getType() == Message.Type.USER)
            .collect(Collectors.toList());
    }

}
