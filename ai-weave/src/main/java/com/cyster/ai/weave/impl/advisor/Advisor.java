package com.cyster.ai.weave.impl.advisor;

import com.cyster.ai.weave.service.conversation.ActiveConversation;

public interface Advisor<C> {

    String getName();

    AdvisorConversationBuilder<C> createConversation();

    interface AdvisorConversationBuilder<C> {

        AdvisorConversationBuilder<C> withContext(C context);

        AdvisorConversationBuilder<C> setOverrideInstructions(String instruction);

        AdvisorConversationBuilder<C> addMessage(String message);

        ActiveConversation start();
    }
}
