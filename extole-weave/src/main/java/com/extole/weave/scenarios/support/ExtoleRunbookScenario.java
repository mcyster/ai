package com.extole.weave.scenarios.support;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.advisors.runbooks.ExtoleRunbookAdvisor;


@Component
public class ExtoleRunbookScenario implements Scenario<Void, Void> {
    public static String NAME = "extoleRunbook";

    private Advisor<Void> advisor; 

    ExtoleRunbookScenario(ExtoleRunbookAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Find the best Runbook for the current prompt context";
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


