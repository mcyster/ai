package com.extole.app.jira.conversation;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record ConvenienceConversationResponse(
    ConversationResponse conversationResponse, 
    String response) {

    public ConvenienceConversationResponse {
        if (conversationResponse == null) {
            throw new IllegalArgumentException("ConversationResponse cannot be null");
        }
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("Response cannot be null or blank");
        }
    }

    public String getId() {
        return conversationResponse.id();
    }

    public String getScenario() {
        return conversationResponse.scenario();
    }

    public List<MessageResponse> getMessages() {
        return conversationResponse.messages();
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
}
