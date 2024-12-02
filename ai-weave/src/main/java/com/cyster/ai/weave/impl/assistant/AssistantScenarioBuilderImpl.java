package com.cyster.ai.weave.impl.assistant;

import java.util.Optional;

import com.cyster.ai.weave.impl.advisor.AdvisorBuilder;
import com.cyster.ai.weave.impl.advisor.assistant.AssistantAdvisorImpl;
import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolContextFactory;
import com.cyster.ai.weave.service.scenario.Scenario;

public class AssistantScenarioBuilderImpl<PARAMETERS, CONTEXT>
        implements AssistantScenarioBuilder<PARAMETERS, CONTEXT> {
    private String name;
    private AdvisorBuilder<CONTEXT> advisorBuilder;
    private Optional<String> description = Optional.empty();

    private Class<PARAMETERS> parameterClass;
    private Class<CONTEXT> contextClass;

    public AssistantScenarioBuilderImpl(OpenAiService openAiService, ToolContextFactory toolContextFactory,
            String name) {
        this.name = name;
        this.advisorBuilder = new AssistantAdvisorImpl.Builder<CONTEXT>(openAiService, toolContextFactory, name);
    }

    public AssistantScenarioBuilder<PARAMETERS, CONTEXT> setDescription(String description) {
        this.description = Optional.of(description);
        return this;
    }

    @Override
    public AssistantScenarioBuilder<PARAMETERS, CONTEXT> setInstructions(String instructions) {
        this.advisorBuilder.setInstructions(instructions);
        return this;
    }

    @Override
    public AssistantScenarioBuilder<PARAMETERS, CONTEXT> withTool(Tool<?, ?> tool) {
        this.advisorBuilder.withTool(tool);
        return this;
    }

    @Override
    public Scenario<PARAMETERS, CONTEXT> getOrCreate() {
        if (description.isEmpty()) {
            description = Optional.of(name);
        }
        return new AssistantScenario<PARAMETERS, CONTEXT>(advisorBuilder.getOrCreate(), description.get(),
                parameterClass, contextClass);
    }

}
