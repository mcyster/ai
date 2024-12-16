package com.cyster.ai.weave.service;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.conversation.Conversation;

public interface WeaveContext {

    Conversation conversation();

    OperationLogger logger();
}
