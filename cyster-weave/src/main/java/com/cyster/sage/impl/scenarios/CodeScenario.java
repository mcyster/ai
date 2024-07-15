package com.cyster.sage.impl.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.sage.impl.advisors.CodingAdvisor;

@Component
public class CodeScenario implements Scenario<Void, Void> {
    private static final String NAME = "code";
    
    private Advisor<Void> advisor;
    
    CodeScenario(CodingAdvisor codingAdvisor) {
        this.advisor = codingAdvisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Not sure what this does";
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
