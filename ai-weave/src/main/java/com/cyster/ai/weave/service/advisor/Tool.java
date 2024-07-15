package com.cyster.ai.weave.service.advisor;


public interface Tool<T, C> {

    String getName();

    String getDescription();

    Class<T> getParameterClass();

    Object execute(T parameters, C context) throws ToolException;
}
