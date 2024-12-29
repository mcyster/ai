package com.cyster.ai.weave.service.scenario;

import java.util.Objects;

public interface Scenario<PARAMETERS, CONTEXT> {

    String getName();

    String getDescription();

    Class<PARAMETERS> getParameterClass();

    Class<CONTEXT> getContextClass();

    // Object execute(PARAMETERS parameters, CONTEXT context, WeaveContext
    // weaveContext) throws ToolException;

    ConversationBuilder createConversationBuilder(PARAMETERS parameters, CONTEXT context);

    default int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass(), getContextClass());
    }

    interface ConversationBuilder {
        ConversationBuilder setOverrideInstructions(String instructions);

        // TODO ConversationBuilder addInstruction(String instruction);
        ConversationBuilder addMessage(String message);

        ScenarioConversation start();
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
