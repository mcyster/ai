package com.cyster.ai.weave.impl.advisor.assistant;

import com.cyster.ai.weave.service.conversation.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageImpl implements Message {
    private final Type type;
    private final String content;
    private final WeaveOperation operation;

    public MessageImpl(Type type, String content, WeaveOperation operation) {
        this.type = type;
        this.content = content;
        this.operation = operation;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public WeaveOperation operation() {
        return this.operation;
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }
}
