package com.cyster.weave.impl.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.template.StringTemplate;
import com.cyster.weave.impl.scenarios.TranslateScenario.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
public class TranslateScenario implements Scenario<Parameters, Void> {
    private ChatScenario chatScenario;

    TranslateScenario(ChatScenario chatScenario) {
        this.chatScenario = chatScenario;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Scenario", "");
    }

    @Override
    public String getDescription() {
        return "Translates a message from one language to another";
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public ActiveConversationBuilder<Void> createConversationBuilder(Parameters parameters, Void context) {
        String instructionsTemplate = "Please translate messages from {{language}} to {{target_language}}.";

        String instructions = new StringTemplate(instructionsTemplate).render(parameters);

        return this.chatScenario.createConversationBuilder(null, context).setOverrideInstructions(instructions);
    }

    static record Parameters(
            @JsonPropertyDescription("language to translate from, ISO 639-1, defaults to en") @JsonProperty(required = false) String language,
            @JsonPropertyDescription("the language to which to translate the text, ISO 639-1") @JsonProperty(required = true) String target_language) {
    }

}
