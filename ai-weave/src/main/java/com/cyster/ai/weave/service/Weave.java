package com.cyster.ai.weave.service;

import com.cyster.ai.weave.impl.advisor.assistant.WeaveOperation;
import com.cyster.ai.weave.service.conversation.Conversation;

public interface Weave {

    Conversation conversation();

    WeaveOperation operation();

}
