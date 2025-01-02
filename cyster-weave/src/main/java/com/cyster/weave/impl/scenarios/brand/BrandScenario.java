package com.cyster.weave.impl.scenarios.brand;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
@ConditionalOnBean(BrandFetchTool.class)
public class BrandScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Provides details about company brands";

    private final Advisor<Void> advisor;

    public BrandScenario(AiAdvisorService aiAdvisorService, BrandFetchTool brandFetchTool,
            BrandSearchTool brandSearchTool) {

        var instructions = """
                You focus on find details on Company brands.
                """;

        AdvisorBuilder<Void> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(instructions);
        builder.withTool(brandSearchTool);

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
