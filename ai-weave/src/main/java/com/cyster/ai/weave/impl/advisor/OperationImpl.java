package com.cyster.ai.weave.impl.advisor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.cyster.ai.weave.service.conversation.Operation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OperationImpl implements Operation, OperationLogger {
    private final String description;
    private final Optional<Object> context;
    private final List<Operation> children;
    
    public OperationImpl(String description, Object context, List<Operation> children) {
        this.description = description;
        this.context = Optional.of(context);
        this.children = children;
    }

    public OperationImpl(String description, Object context) {
        this.description = description;
        this.context = Optional.of(context);
        this.children = new CopyOnWriteArrayList<>();
    }

    public OperationImpl(String description) {
        this.description = description;
        this.context = Optional.empty();
        this.children = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public String getDescription() {
        return description;
    }


    @Override
    public List<Operation> children() {
        return children;
    }

    @Override
    public Optional<Object> context() {
       return context;
    }
    
    @Override
    public void log(String description, Object context) {
       children.add(new OperationImpl(description, context));
    }

    @Override
    public OperationLogger childLogger(String description) {
        var operation = new OperationImpl(description);
        children.add(operation);
        return operation;
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