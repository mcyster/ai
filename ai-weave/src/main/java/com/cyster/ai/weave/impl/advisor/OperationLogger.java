package com.cyster.ai.weave.impl.advisor;

import com.cyster.ai.weave.service.conversation.Operation.Level;

public interface OperationLogger {
    void log(String description, Object context);
    void log(Level level, String description, Object context);

    OperationLogger childLogger(String description); 
    OperationLogger childLogger(Level level, String description); 
    
}
