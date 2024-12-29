package com.cyster.ai.weave.impl.scenario;

import java.util.Optional;

import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.AiAdvisorService;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioBuilder;
import com.cyster.ai.weave.service.tool.Tool;

public class ScenarioBuilderImpl<PARAMETERS, CONTEXT> implements ScenarioBuilder<PARAMETERS, CONTEXT> {
    private String name;
    private AdvisorBuilder<CONTEXT> advisorBuilder;
    private Optional<String> description = Optional.empty();

    private Class<PARAMETERS> parameterClass;
    private Class<CONTEXT> contextClass;

    public ScenarioBuilderImpl(AiAdvisorService advisorService, String name) {
        this.name = name;
        this.advisorBuilder = advisorService.getOrCreateAdvisorBuilder(name);
    }

    public ScenarioBuilder<PARAMETERS, CONTEXT> setDescription(String description) {
        this.description = Optional.of(description);
        return this;
    }

    @Override
    public ScenarioBuilder<PARAMETERS, CONTEXT> setInstructions(String instructions) {
        this.advisorBuilder.setInstructions(instructions);
        return this;
    }

    @Override
    public ScenarioBuilder<PARAMETERS, CONTEXT> withTool(Tool<?, ?> tool) {
        this.advisorBuilder.withTool(tool);
        return this;
    }

    @Override
    public Scenario<PARAMETERS, CONTEXT> getOrCreate() {
        if (description.isEmpty()) {
            description = Optional.of(name);
        }
        return new ScenarioImpl<PARAMETERS, CONTEXT>(advisorBuilder.getOrCreate(), description.get(), parameterClass,
                contextClass);
    }

}
