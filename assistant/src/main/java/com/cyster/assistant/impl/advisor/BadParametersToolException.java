package com.cyster.assistant.impl.advisor;

import com.cyster.assistant.service.advisor.ToolException;

public class BadParametersToolException extends ToolException {

    public BadParametersToolException(String message) {
        super(message);
    }

    public BadParametersToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
