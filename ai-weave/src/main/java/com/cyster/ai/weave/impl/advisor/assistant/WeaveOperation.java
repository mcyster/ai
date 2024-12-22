package com.cyster.ai.weave.impl.advisor.assistant;

import com.cyster.ai.weave.service.conversation.Operation;

public interface WeaveOperation extends Operation {
    void log(String description, Object context);
    void log(Level level, String description, Object context);

    WeaveOperation childLogger(String description);
    WeaveOperation childLogger(String description, Object context);
    WeaveOperation childLogger(Level level, String description);

}
