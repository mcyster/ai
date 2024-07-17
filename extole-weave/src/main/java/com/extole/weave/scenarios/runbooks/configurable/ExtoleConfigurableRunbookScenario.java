package com.extole.weave.scenarios.runbooks.configurable;

import java.io.StringReader;
import java.io.StringWriter;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.extole.weave.scenarios.runbooks.RunbookScenario;
import com.extole.weave.scenarios.runbooks.RunbookScenarioParameters;
import com.extole.weave.scenarios.support.ExtoleSupportHelpScenario;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class ExtoleConfigurableRunbookScenario implements RunbookScenario {
    private String name;
    private String description;
    private String keywords;
    private String instructions;
    
    private ExtoleSupportHelpScenario helpScenario;

    ExtoleConfigurableRunbookScenario(String name, Configuration configuration, ExtoleSupportHelpScenario helpScenario) {
        this.name = name;
        this.description = configuration.getDescription();
        this.keywords = configuration.getKeywords();
        this.instructions = configuration.getInstructions();
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
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public ConversationBuilder createConversationBuilder(RunbookScenarioParameters parameters, Void context) {
        MustacheFactory mostacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mostacheFactory.compile(new StringReader(instructions), "instructions");
        var messageWriter = new StringWriter();
        mustache.execute(messageWriter, parameters);
        messageWriter.flush();

        var interpretedInstructions = messageWriter.toString();
                
        System.out.println("!!!! Runbook: " + this.name + " instructions" + interpretedInstructions);

        return this.helpScenario.createConversationBuilder(null, null).setOverrideInstructions(interpretedInstructions);
    }
    
    public static class Configuration {
        private String description;
        private String keywords;
        private String instructions;

        @JsonCreator
        public Configuration(
                @JsonProperty("description") String description,
                @JsonProperty("keywords") String keywords,
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

