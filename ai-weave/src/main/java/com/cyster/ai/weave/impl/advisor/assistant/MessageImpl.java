package com.cyster.ai.weave.impl.advisor.assistant;

import com.cyster.ai.weave.service.conversation.Operation;

import com.cyster.ai.weave.service.conversation.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageImpl implements Message {
    private final Type type;
    private final String content;
    private final Operation operation;

    public MessageImpl(Type type, String content, Operation operation) {
        this.type = type;
        this.content = content;
        this.operation = operation;
    }

    public MessageImpl(Type type, String content) {
        this.type = type;
        this.content = content;
        this.operation = new OperationImpl("operations");
    }

    public MessageImpl(String content) {
        this.type = Type.USER;
        this.content = content;
        this.operation = new OperationImpl("operations");
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
    public Operation operation() {
        return this.operation;
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
