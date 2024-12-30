package com.cyster.weave.impl.scenarios.webshot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
@ConditionalOnBean(WebshotTool.class)
public class WebshotScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Turns the specified url into an image";

    private final Advisor<Void> advisor;

    public WebshotScenario(AiAdvisorService aiAdvisorService, WebshotTool webshotTool) {

        var instructions = """
                You take snapshots of web pages.
                """;

        AdvisorBuilder<Void> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(instructions).withTool(webshotTool).withTool(webshotTool);

        builder.withTool(webshotTool);

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
    public ActiveConversationBuilder<Void> createConversationBuilder(Void parameters, Void context) {
        return this.advisor.createConversationBuilder(context);
    }

}
