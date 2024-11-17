package com.cyster.weave.impl.scenarios.webshot;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
@ConditionalOnBean(WebshotTool.class)
public class WebshotScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Turns the specified url into an image";
    private AiWeaveService aiWeaveService;
    private WebshotTool webshotTool;
    private final AtomicReference<Scenario<Void, Void>> scenario = new AtomicReference<>();

    public WebshotScenario(AiWeaveService aiWeaveService, WebshotTool webshotTool) {
        this.aiWeaveService = aiWeaveService;
        this.webshotTool = webshotTool;
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
    public ConversationBuilder createConversationBuilder(Void parameters, Void context) {
        return this.getScenario().createConversationBuilder(parameters, context);
    }

    private Scenario<Void, Void> getScenario() {
        return scenario.updateAndGet(existing -> {
            if (existing == null) {
                var instructions = """
                        You take snapshots of web pages.
                        """;

                AssistantScenarioBuilder<Void, Void> builder = this.aiWeaveService
                        .getOrCreateAssistantScenario(getName());

                builder.setInstructions(instructions).withTool(webshotTool).withTool(webshotTool);

                return builder.getOrCreate();
            }
            return existing;
        });
    }
}
