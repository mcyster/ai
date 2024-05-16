package com.cyster.assistant.service.advisor;

import com.cyster.assistant.impl.advisor.ToolException;

public class FatalToolException extends ToolException {
    
    public FatalToolException(String message) {
        super(message);
    }
    
    public FatalToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
