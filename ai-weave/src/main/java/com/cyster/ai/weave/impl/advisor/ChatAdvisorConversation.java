package com.cyster.ai.weave.impl.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;

import io.github.stefanbratanov.jvm.openai.ChatClient;
import io.github.stefanbratanov.jvm.openai.ChatMessage;
import io.github.stefanbratanov.jvm.openai.CreateChatCompletionRequest;
import io.github.stefanbratanov.jvm.openai.OpenAI;

public class ChatAdvisorConversation implements Conversation {
    private final String MODEL = "gpt-4o";

    private OpenAI openAi;
    private List<Message> messages;
    
    ChatAdvisorConversation(OpenAI openAi, List<Message> messages) {
        this.openAi = openAi;   
        this.messages = messages;
    }
    
    @Override
    public Conversation addMessage(String message) {
        this.messages.add(new MessageImpl(message));
        return this;
    }

    @Override
    public Message respond() throws ConversationException {
        ChatClient chatClient = this.openAi.chatClient();

        var chatMessages = new ArrayList<ChatMessage>(); 

        for (var message : this.messages) {
            if (message.getType() == Message.Type.SYSTEM) {
                chatMessages.add(ChatMessage.systemMessage(message.getContent()));
            } else if (message.getType() == Message.Type.AI) {
                chatMessages.add(ChatMessage.assistantMessage(message.getContent()));
            } else if (message.getType() == Message.Type.USER) {
                chatMessages.add(ChatMessage.userMessage(message.getContent()));
            }
        }

        var requestBuilder = CreateChatCompletionRequest.newBuilder()
            .model(MODEL)
            .messages(chatMessages);

        var result = chatClient.createChatCompletion(requestBuilder.build());

        var choices = result.choices();
        if (choices.size() == 0) {
            messages.add(new MessageImpl(Message.Type.INFO, "No responses"));
            throw new ConversationException("No Reponses");
        } 
        if (choices.size() > 1) {
            messages.add(new MessageImpl(Message.Type.INFO, "Multiple responses (ignored)"));
            throw new ConversationException("Multiple Reponses");
        }
        
        var message = new MessageImpl(Message.Type.AI, choices.get(0).message().content());
        messages.add(message);
        return message;
    }

    @Override
    public List<Message> getMessages() {
        return messages.stream()
            .filter(message -> message.getType() == Message.Type.AI || message.getType() == Message.Type.USER)
            .collect(Collectors.toList());
    }

}
