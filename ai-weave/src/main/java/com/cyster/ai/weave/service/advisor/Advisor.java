package com.cyster.ai.weave.service.advisor;

import com.cyster.ai.weave.service.conversation.Conversation;

public interface Advisor<C> {

    String getName();

    AdvisorConversationBuilder<C> createConversation();

    interface AdvisorConversationBuilder<C> {

        AdvisorConversationBuilder<C> withContext(C context);

        AdvisorConversationBuilder<C> setOverrideInstructions(String instruction);

        AdvisorConversationBuilder<C> addMessage(String message);

        Conversation start();
    }
}
