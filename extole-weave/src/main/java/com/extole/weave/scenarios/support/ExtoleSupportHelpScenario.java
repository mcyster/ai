package com.extole.weave.scenarios.support;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.advisors.support.ExtoleSupportAdvisor;

@Component
public class ExtoleSupportHelpScenario implements Scenario<Void, Void> {
    public static String NAME = "extoleSupportHelp";

    private Advisor<Void> advisor;

    ExtoleSupportHelpScenario(ExtoleSupportAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Help with the Extole platform for members of the Extole Support Team";
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
        return this.advisor.createConversation().start();
    }

    @Override
    public ConversationBuilder createConversationBuilder(Void parameters, Void context) {
        // TODO Auto-generated method stub
        return null;
    }

}
