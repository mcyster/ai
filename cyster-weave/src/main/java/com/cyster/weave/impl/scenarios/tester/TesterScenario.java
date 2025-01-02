package com.cyster.weave.impl.scenarios.tester;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
public class TesterScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Helps with testing scenarios";

    private final Advisor<Void> advisor;

    public TesterScenario(AiAdvisorService aiAdvisorService, RandomNumberTool randomNumberTool,
            FailingTesterTool failingTesterTool, NestedAiTool nestedAiTool) {

        AdvisorBuilder<Void> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions("You are a helpful assistant.");

        builder.withTool(randomNumberTool);
        builder.withTool(failingTesterTool);
        builder.withTool(nestedAiTool);

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
