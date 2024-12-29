package com.cyster.ai.weave.service;

import com.cyster.ai.weave.impl.WeaveOperation;
import com.cyster.ai.weave.service.conversation.ActiveConversation;

public interface Weave {

    ActiveConversation conversation();

    WeaveOperation operation();

}
