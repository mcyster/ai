package com.extole.zuper.weave.scenarios.runbooks.configurable;

import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.template.StringTemplate;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.runbooks.RunbookSuperScenario;
import com.extole.zuper.weave.scenarios.runbooks.RunbookScenarioParameters;
import com.extole.zuper.weave.scenarios.support.ExtoleSupportHelpSuperScenario;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtoleConfigurableRunbookSuperScenario implements RunbookSuperScenario {
    private String name;
    private String description;
    private String keywords;
    private String instructionsTemplate;

    private ExtoleSupportHelpSuperScenario helpScenario;

    ExtoleConfigurableRunbookSuperScenario(String name, Configuration configuration,
            ExtoleSupportHelpSuperScenario helpScenario) {
        this.name = name;
        this.description = configuration.getDescription();
        this.keywords = configuration.getKeywords();
        this.instructionsTemplate = configuration.getInstructions();
        this.helpScenario = helpScenario;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public String getKeywords() {
        return this.keywords;
    }

    @Override
    public Class<RunbookScenarioParameters> getParameterClass() {
        return RunbookScenarioParameters.class;
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(RunbookScenarioParameters parameters,
            ExtoleSuperContext context) {
        String instructions = new StringTemplate(instructionsTemplate).render(parameters);

        System.out.println("!!!! Runbook: " + this.name + " instructions" + instructions);

        return this.helpScenario.createConversationBuilder(null, context).setOverrideInstructions(instructions);
    }

    public static class Configuration {
        private String description;
        private String keywords;
        private String instructions;

        @JsonCreator
        public Configuration(@JsonProperty("description") String description, @JsonProperty("keywords") String keywords,
                @JsonProperty("instructions") String instructions) {
            setDescription(description);
            setKeywords(keywords);
            setInstructions(instructions);
        }

        public String getDescription() {
            return description;
        }

        private void setDescription(String description) {
            validateString(description, "description");
            this.description = description;
        }

        public String getKeywords() {
            return keywords;
        }

        private void setKeywords(String keywords) {
            validateString(keywords, "keywords");
            this.keywords = keywords;
        }

        public String getInstructions() {
            return instructions;
        }

        private void setInstructions(String instructions) {
            validateString(instructions, "instructions");
            this.instructions = instructions;
        }

        private void validateString(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(fieldName + " cannot be null or empty");
            }
        }
    }

}
