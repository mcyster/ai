package com.cyster.weave.impl.scenarios;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.weave.impl.scenarios.TranslateScenario.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class TranslateScenario implements Scenario<Parameters, Void> {
    private static final String NAME = "translate";
    private ChatScenario chatScenario;

    TranslateScenario(ChatScenario chatScenario) {
        this.chatScenario = chatScenario;
    }

    @Override
    public String getName() {
        return NAME;
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
    public Conversation createConversation(Parameters parameters, Void context) {
        throw new UnsupportedOperationException("Method is deprectated and being removed from interface");
    }

    
    @Override
    public ConversationBuilder createConversationBuilder(Parameters parameters, Void context) {
        String systemPrompt = "Please translate messages from {{language}} to {{target_language}}.";

        MustacheFactory mostacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
        var messageWriter = new StringWriter();
        mustache.execute(messageWriter, parameters);
        messageWriter.flush();
        var instructions = messageWriter.toString();

        return this.chatScenario.createConversationBuilder(null, context)
            .setOverrideInstructions(instructions);
    }
    
    static class Parameters {
        @JsonPropertyDescription("language to translate from, ISO 639-1, defaults to en")
        @JsonProperty(required = false)
        public String language;

        @JsonPropertyDescription("the language to which to translate the text, ISO 639-1")
        @JsonProperty(required = true)
        public String target_language;
    }

}
