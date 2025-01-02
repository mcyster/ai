package com.cyster.ai.weave.service.conversation;

public interface ActiveConversationBuilder {

    ActiveConversationBuilder setOverrideInstructions(String instruction);

    ActiveConversationBuilder addMessage(String message);

    ActiveConversation start();
}