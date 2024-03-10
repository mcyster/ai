package com.extole.sage.scenarios.support;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.support.ExtoleSupportAdvisor;

@Component
public class ExtoleSupportHelpScenario implements Scenario<Void, Void> {
    public static String NAME = "extole_support_help";
    
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
    
}
