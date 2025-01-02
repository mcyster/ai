package com.cyster.scheduler.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
public class SchedulerScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Schedules execution of scenarios in the future";

    private final Advisor<Void> advisor;

    @Autowired
    public SchedulerScenario(AiAdvisorService aiAdvisorService, SchedulerTool schedulerTool) {

        var instructions = """
                Your job is focused on scheduling the execution of scenarios in the future
                """;

        AdvisorBuilder<Void> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(instructions).withTool(schedulerTool);

        this.advisor = builder.getOrCreate();
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
    public ActiveConversationBuilder createConversationBuilder(Void parameters, Void context) {
        return this.advisor.createConversationBuilder(context);
    }

}
