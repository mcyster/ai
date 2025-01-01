package com.cyster.ai.weave.service.tool;

import java.util.Objects;

import com.cyster.ai.weave.service.Weave;

public interface Tool<PARAMETERS, CONTEXT> {

    String getName();

    String getDescription();

    Class<PARAMETERS> getParameterClass();

    Class<CONTEXT> getContextClass();

    Object execute(PARAMETERS parameters, CONTEXT context, Weave weave) throws ToolException;

    default int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass());
    }
}
