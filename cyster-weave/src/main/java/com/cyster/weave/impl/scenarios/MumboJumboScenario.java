package com.cyster.weave.impl.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.weave.impl.advisors.MumboJumboAdvisor;

@Component
public class MumboJumboScenario implements Scenario<Void, Void> {
    private static final String NAME = "mumboJumbo";
    private Advisor<Void> advisor;

    MumboJumboScenario(MumboJumboAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Mumbo Jumbo stuff";
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
        String instructions = "Generate 10 made up words and their definitions";

        return this.advisor.createConversation()
            .setOverrideInstructions(instructions)
            .start();
    }

    @Override
    public ConversationBuilder createConversationBuilder(Void parameters, Void context) {
        // TODO Auto-generated method stub
        return null;
    }


}
