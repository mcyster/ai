package com.cyster.weave.impl.scenarios.tester;

import java.util.Random;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.weave.impl.scenarios.tester.RandomNumberTool.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
public class RandomNumberTool implements Tool<Parameters, Void> {
    private Random random;

    RandomNumberTool() {
        this.random = new Random();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Generates a random number";
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    };

    @Override
    public Object execute(Parameters parameters, Void context, OperationLogger operation) throws ToolException {
        int lowerBound = 0;
        if (parameters.lowerBound != null) {
            lowerBound = parameters.lowerBound;
        }
        int upperBound = 100;
        if (parameters.upperBound != null) {
            upperBound = parameters.upperBound;
        }

        Integer value = this.random.nextInt(upperBound - lowerBound + 1) + lowerBound;

        return value;
    }

    public static record Parameters(
            @JsonPropertyDescription("Lower bound of random number, defaults to 0") @JsonProperty(required = false) Integer lowerBound,
            @JsonPropertyDescription("Upper bound of random number, defaults to 100") @JsonProperty(required = false) Integer upperBound) {
    }

}
