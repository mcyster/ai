package com.cyster.ai.weave.service.scenario;

import java.util.Objects;

import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;

public interface Scenario<PARAMETERS, CONTEXT> {

    String getName();

    String getDescription();

    Class<PARAMETERS> getParameterClass();

    Class<CONTEXT> getContextClass();

    // Object execute(PARAMETERS parameters, CONTEXT context, WeaveContext
    // weaveContext) throws ToolException;

    ActiveConversationBuilder createConversationBuilder(PARAMETERS parameters, CONTEXT context);

    default int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass(), getContextClass());
    }

    default ScenarioType toScenarioType() {
        return new ScenarioType() {
            @Override
            public String name() {
                return Scenario.this.getName();
            }

            @Override
            public String description() {
                return Scenario.this.getDescription();
            }

            @Override
            public Class<?> parameterClass() {
                return Scenario.this.getParameterClass();
            }

            @Override
            public Class<?> contextClass() {
                return Scenario.this.getContextClass();
            }
        };
    }

}
