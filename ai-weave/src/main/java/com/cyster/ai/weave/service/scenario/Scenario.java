package com.cyster.ai.weave.service.scenario;

import com.cyster.ai.weave.service.conversation.Conversation;

public interface Scenario<PARAMETERS, CONTEXT> {

    String getName();

    String getDescription();

    Class<PARAMETERS> getParameterClass();

    Class<CONTEXT> getContextClass();

    Conversation createConversation(PARAMETERS parameters, CONTEXT context);
}
