package com.cyster.weave.impl.scenarios.tester;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiScenarioService;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioBuilder;

@Component
public class NestedTesterScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Helps with testing nested scenarios";

    private final Scenario<Void, Void> scenario;

    public NestedTesterScenario(AiScenarioService aiScenarioService, RandomNumberTool randomNumberTool,
            FailingTesterTool failingTesterTool) {

        ScenarioBuilder<Void, Void> builder = aiScenarioService.getOrCreateScenarioBuilder(getName());

        builder.setInstructions("You are a helpful assistant.");

        builder.withTool(randomNumberTool);
        builder.withTool(failingTesterTool);

        this.scenario = builder.getOrCreate();

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
        // todo nest Weave context
        return this.scenario.createConversationBuilder(parameters, context);
    }
}
