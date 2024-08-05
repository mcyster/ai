package com.cyster.ai.weave.impl.advisor.assistant;

import com.cyster.ai.weave.service.conversation.Operation;

public interface OperationLogger extends Operation {
    void log(String description, Object context);
    void log(Level level, String description, Object context);

    OperationLogger childLogger(String description);
    OperationLogger childLogger(String description, Object context);
    OperationLogger childLogger(Level level, String description);

}
