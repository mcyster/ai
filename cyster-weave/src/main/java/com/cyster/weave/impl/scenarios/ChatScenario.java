package com.cyster.weave.impl.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.weave.impl.advisors.SimpleAssistantScenario;


@Component
public class ChatScenario implements Scenario<Void, Void> {
    private static final String NAME = "chat";
    private SimpleAssistantScenario scenario;


    ChatScenario(SimpleAssistantScenario scenario) {
        this.scenario = scenario;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Chat with the AI";
    }

    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Void parameters, Void context) {
        throw new UnsupportedOperationException("Method is deprectated and being removed from interface");
    }
    
    @Override
    public ConversationBuilder createConversationBuilder(Void parameters, Void context) {
        String instructions = "Enjoy a chat, say hi if there is no prompt.";

        return this.scenario.createConversationBuilder(parameters, context)
            .setOverrideInstructions(instructions);
    }


}
