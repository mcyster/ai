package com.cyster.ai.weave.service.advisor;

public class FatalToolException extends ToolException {
    
    public FatalToolException(String message) {
        super(message);
    }
    
    public FatalToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
