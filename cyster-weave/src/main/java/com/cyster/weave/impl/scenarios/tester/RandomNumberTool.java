package com.cyster.weave.impl.scenarios.tester;

import java.util.Random;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import com.cyster.weave.impl.scenarios.tester.RandomNumberTool.Parameters;

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
        return "Fails - to allow testing";
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Object execute(Parameters parameters, Void context) throws ToolException {
        int value = this.random.nextInt(parameters.upperBound - parameters.lowerBound + 1) + parameters.lowerBound;
        
        return value;
    }
    
    public static record Parameters(
        @JsonPropertyDescription("Lower bound of random number, defaults to 0")
        @JsonProperty(required = false)
        int lowerBound,
        @JsonPropertyDescription("Upper bound of random number, defaults to 100")
        @JsonProperty(required = false)
        int upperBound
    ) {};
}




