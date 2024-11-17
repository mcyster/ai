package com.cyster.weave.impl.scenarios;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
public class ChatScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "A helpful assistant";

    private AiWeaveService aiWeaveService;
    private final AtomicReference<Scenario<Void, Void>> scenario = new AtomicReference<>();

    public ChatScenario(AiWeaveService aiWeaveService) {
        this.aiWeaveService = aiWeaveService;
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
                AssistantScenarioBuilder<Void, Void> builder = this.aiWeaveService
                        .getOrCreateAssistantScenario(getName());
                builder.setInstructions("You are a helpful assistant.");
                return builder.getOrCreate();
            }
            return existing;
        });
    }
}
