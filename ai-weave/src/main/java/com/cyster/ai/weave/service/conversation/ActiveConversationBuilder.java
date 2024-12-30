package com.cyster.ai.weave.service.conversation;

public interface ActiveConversationBuilder<CONTEXT> {

    ActiveConversationBuilder<CONTEXT> setOverrideInstructions(String instruction);

    ActiveConversationBuilder<CONTEXT> addMessage(String message);

    ActiveConversation start();
}