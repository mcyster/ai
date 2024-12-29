package com.extole.admin.weave.scenarios.wismr;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiScenarioService;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioBuilder;
import com.extole.admin.weave.session.ExtoleSessionContext;

@Component
public class ExtoleWismrScenario implements Scenario<Void, ExtoleSessionContext> {
    private static final String DESCRIPTION = "Extole tool to help find the reward of a person";

    private AiScenarioService aiScenarioService;
    private Optional<Scenario<Void, ExtoleSessionContext>> scenario = Optional.empty();

    ExtoleWismrScenario(AiScenarioService aiScenarioService, ExtolePersonFindToolFactory extolePersonFindToolFactory,
            ExtolePersonRewardsToolFactory extolePersonRewardsToolFactory,
            ExtolePersonStepsToolFactory extolePersonStepsToolFactory, ExtoleStepsToolFactory extoleStepsToolFactory) {
        this.aiScenarioService = aiScenarioService;
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
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public ActiveConversationBuilder<ExtoleSessionContext> createConversationBuilder(Void parameters,
            ExtoleSessionContext context) {
        return this.getScenario().createConversationBuilder(parameters, context);
    }

    private Scenario<Void, ExtoleSessionContext> getScenario() {
        if (this.scenario.isEmpty()) {
            String instructions = """
                    You are a customer service representative for the Extole SaaS marketing platform.
                    You specialize in helping people find a reward they expected to receive from the Extole platform.

                    Step 1: Identify the person and check if they have any rewards.
                    The best way to start is to try and load and review the persons profile base on a key we might have,
                    such as email, partner_user_id or order_id.

                    Step 2: Person has rewards
                    If the person has rewards, show the rewards, we are done.

                    Step 3: Person has no rewards
                    Get all the stepNames that have an actionName earnRewards.

                    Step 4:
                    Check if the person has have any of the stepNames on their profile that earn rewards.
                    If there are steps, show the steps and we are done.

                    """;

            ScenarioBuilder<Void, ExtoleSessionContext> builder = this.aiScenarioService.getOrCreateScenario(getName());

            builder.setInstructions(instructions);
            // .withTool(this.extolePersonFindToolFactory.create(accessToken))
            // .withTool(this.extolePersonRewardsToolFactory.create(accessToken))
            // .withTool(this.extolePersonStepsToolFactory.create(accessToken))
            // .withTool(this.extoleStepsToolFactory.create(accessToken));

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }

}
