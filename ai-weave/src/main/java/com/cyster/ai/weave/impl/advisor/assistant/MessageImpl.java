package com.cyster.ai.weave.impl.advisor.assistant;

import com.cyster.ai.weave.service.conversation.Operation;

import java.util.Optional;

import com.cyster.ai.weave.service.conversation.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageImpl implements Message {
    private final Type type;
    private final String content;
    private final Optional<Operation> operation;

    public MessageImpl(Type type, String content, Operation operation) {
        this.type = type;
        this.content = content;
        this.operation = Optional.of(operation);
    }

    public MessageImpl(Type type, String content) {
        this.type = type;
        this.content = content;
        this.operation = Optional.empty();
    }

    public MessageImpl(String content) {
        this.type = Type.USER;
        this.content = content;
        this.operation = Optional.empty();
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
    public Optional<Operation> operation() {
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
