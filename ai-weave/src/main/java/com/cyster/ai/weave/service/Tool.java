package com.cyster.ai.weave.service;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;

public interface Tool<T, C> {

    String getName();

    String getDescription();

    Class<T> getParameterClass();

    Object execute(T parameters, C context, OperationLogger operation) throws ToolException;
}
