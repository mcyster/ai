package com.cyster.ai.weave.impl.advisor;

import com.cyster.ai.weave.service.ToolException;

public class BadParametersToolException extends ToolException {

    public BadParametersToolException(String message) {
        super(message);
    }

    public BadParametersToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
