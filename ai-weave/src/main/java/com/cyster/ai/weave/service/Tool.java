package com.cyster.ai.weave.service;

import java.util.Objects;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;

public interface Tool<T, C> {

    String getName();

    String getDescription();

    Class<T> getParameterClass();
    
    Object execute(T parameters, C context, OperationLogger operation) throws ToolException;
    
    default int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass());
    }
}
