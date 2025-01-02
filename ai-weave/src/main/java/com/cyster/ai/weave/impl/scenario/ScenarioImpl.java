package com.cyster.ai.weave.impl.scenario;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;

public class ScenarioImpl<PARAMETERS, CONTEXT> implements Scenario<PARAMETERS, CONTEXT> {
    private Advisor<CONTEXT> advisor;
    private String description;
    private Class<PARAMETERS> parameterClass;
    private Class<CONTEXT> contextClass;

    public ScenarioImpl(Advisor<CONTEXT> advisor, String description, Class<PARAMETERS> parameterClass,
            Class<CONTEXT> contextClass) {
        this.advisor = advisor;
        this.description = description;
        this.parameterClass = parameterClass;
        this.contextClass = contextClass;
    }

    @Override
    public String getName() {
        return this.advisor.getName();
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Class<PARAMETERS> getParameterClass() {
        return this.parameterClass;
    }

    @Override
    public Class<CONTEXT> getContextClass() {
        return this.contextClass;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(PARAMETERS parameters, CONTEXT context) {
        return this.advisor.createConversationBuilder(context);
    }

}
