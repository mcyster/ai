package com.cyster.ai.weave.impl.tool;

import com.cyster.ai.weave.service.tool.ToolException;

public class BadParametersToolException extends ToolException {

    public BadParametersToolException(String message) {
        super(message);
    }

    public BadParametersToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
