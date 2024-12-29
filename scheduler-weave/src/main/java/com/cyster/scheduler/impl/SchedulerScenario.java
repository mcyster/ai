package com.cyster.scheduler.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiScenarioService;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioBuilder;

@Component
public class SchedulerScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Schedules execution of scenarios in the future";
    private AiScenarioService aiScenarioService;
    private SchedulerTool schedulerTool;
    private Optional<Scenario<Void, Void>> scenario = Optional.empty();

    @Autowired
    public SchedulerScenario(AiScenarioService aiScenarioService, SchedulerTool schedulerTool) {
        this.aiScenarioService = aiScenarioService;
        this.schedulerTool = schedulerTool;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Scenario", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public ActiveConversationBuilder<Void> createConversationBuilder(Void parameters, Void context) {
        return this.getScenario().createConversationBuilder(parameters, context);
    }

    private Scenario<Void, Void> getScenario() {
        if (this.scenario.isEmpty()) {
            var instructions = """
                    Your job is focused on scheduling the execution of scenarios in the future
                    """;

            ScenarioBuilder<Void, Void> builder = this.aiScenarioService.getOrCreateScenario(getName());

            builder.setInstructions(instructions).withTool(this.schedulerTool);

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }
}
