package com.cyster.weave.rest.conversation;

import com.cyster.ai.weave.service.conversation.Message.Type;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record ConvenienceMessageResponse(
    String id,
    MessageResponse response) {

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
