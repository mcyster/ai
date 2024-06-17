package com.cyster.ai.weave.impl.advisor;

public interface OperationLogger {
    void log(String description, Object context);
    OperationLogger childLogger(String description); 
}
