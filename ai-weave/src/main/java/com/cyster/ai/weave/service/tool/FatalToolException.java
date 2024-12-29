package com.cyster.ai.weave.service.tool;

public class FatalToolException extends ToolException {

    public FatalToolException(String message) {
        super(message);
    }

    public FatalToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
