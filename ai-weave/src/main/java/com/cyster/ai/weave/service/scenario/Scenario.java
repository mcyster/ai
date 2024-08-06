package com.cyster.ai.weave.service.scenario;

import java.util.Objects;

import com.cyster.ai.weave.service.conversation.Conversation;

public interface Scenario<PARAMETERS, CONTEXT> {

    String getName();

    String getDescription();

    Class<PARAMETERS> getParameterClass();

    Class<CONTEXT> getContextClass();
    
    ConversationBuilder createConversationBuilder(PARAMETERS parameters, CONTEXT context);

    default int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass(), getContextClass());
    }
    
    interface ConversationBuilder {
        ConversationBuilder setOverrideInstructions(String instructions);
        // TODO ConversationBuilder addInstruction(String instruction);  
        ConversationBuilder addMessage(String message);
        Conversation start();
    }
}
