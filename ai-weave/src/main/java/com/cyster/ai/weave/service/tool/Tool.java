package com.cyster.ai.weave.service.tool;

import java.util.Objects;

import com.cyster.ai.weave.service.Weave;

public interface Tool<TOOL_PARAMETERS, TOOL_CONTEXT> {

    String getName();

    String getDescription();

    Class<TOOL_PARAMETERS> getParameterClass();

    Class<TOOL_CONTEXT> getContextClass();

    Object execute(TOOL_PARAMETERS parameters, TOOL_CONTEXT context, Weave weave) throws ToolException;

    default int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass());
    }
}
