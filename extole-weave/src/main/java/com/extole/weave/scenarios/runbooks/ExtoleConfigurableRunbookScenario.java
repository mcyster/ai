package com.extole.weave.scenarios.runbooks;

import java.io.StringReader;
import java.io.StringWriter;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.extole.weave.advisors.support.ExtoleSupportAdvisor;
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
    private ExtoleSupportAdvisor advisor;

    ExtoleConfigurableRunbookScenario(Configuration configuration, ExtoleSupportAdvisor advisor) {
        this.name = configuration.getName();
        this.description = configuration.getDescription();
        this.keywords = configuration.getKeywords();
        this.instructions = configuration.getInstructions();
        this.advisor = advisor;
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
    public Conversation createConversation(RunbookScenarioParameters parameters, Void context) {
        MustacheFactory mostacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mostacheFactory.compile(new StringReader(instructions), "advisor_instructions");
        var messageWriter = new StringWriter();
        mustache.execute(messageWriter, parameters);
        messageWriter.flush();

        return this.advisor.createConversation().setOverrideInstructions(messageWriter.toString()).start();
    }

    public static class Configuration {
        private String name;
        private String description;
        private String keywords;
        private String instructions;

        @JsonCreator
        public Configuration(
                @JsonProperty("name") String name,
                @JsonProperty("description") String description,
                @JsonProperty("keywords") String keywords,
                @JsonProperty("instructions") String instructions) {
            setName(name);
            setDescription(description);
            setKeywords(keywords);
            setInstructions(instructions);
        }

        public String getName() {
             return name;
        }

        private void setName(String name) {
            validateString(name, "name");

            if (!name.matches("[a-zA-Z0-9]+")) {
                throw new IllegalArgumentException("name must only contain alphanumeric characters");
            }

            this.name = "extoleRunbook" + name.substring(0, 1).toUpperCase() + name.substring(1);

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

    @Override
    public ConversationBuilder createConversationBuilder(RunbookScenarioParameters parameters, Void context) {
        // TODO Auto-generated method stub
        return null;
    }
}

