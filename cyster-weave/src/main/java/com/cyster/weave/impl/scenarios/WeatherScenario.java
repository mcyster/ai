package com.cyster.weave.impl.scenarios;

import java.io.StringReader;
import java.io.StringWriter;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.weave.impl.advisors.WeatherAdvisor;
import com.cyster.weave.impl.scenarios.WeatherScenario.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class WeatherScenario implements Scenario<Parameters, Void> {
    private static final String NAME = "weather";

    private Advisor<Void> advisor;

    WeatherScenario(WeatherAdvisor weatherAdvisor) {
        this.advisor = weatherAdvisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Gives a weather at a location";
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
        String systemPrompt = "Describe the weather in {{location}} in units of {{unit}}.";

        MustacheFactory mostacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
        var messageWriter = new StringWriter();
        mustache.execute(messageWriter, parameters);
        messageWriter.flush();
        var instructions = messageWriter.toString();

        var conversationBuilder  = this.advisor.createConversation()
            .setOverrideInstructions(instructions);

        return conversationBuilder.start();
    }

    public static class Parameters {
        @JsonPropertyDescription("City and state, for example: León, Guanajuato")
        public String location;

        @JsonPropertyDescription("The temperature unit, can be 'celsius' or 'fahrenheit'")
        @JsonProperty(required = true)
        public WeatherUnit unit;
    }

    public enum WeatherUnit {
        CELSIUS, FAHRENHEIT;
    }

}
