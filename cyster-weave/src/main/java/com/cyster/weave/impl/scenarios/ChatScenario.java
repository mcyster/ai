package com.cyster.weave.impl.scenarios;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.ScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.weave.impl.scenarios.conversation.ConversationLinkTool;

@Component
public class ChatScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "A helpful assistant";

    private final AiService aiWeaveService;
    private final List<Tool<?, ?>> tools = new ArrayList<>();
    private final AtomicReference<Scenario<Void, Void>> scenario = new AtomicReference<>();

    public ChatScenario(AiService aiWeaveService, ConversationLinkTool conversationLinkTool) {
        this.aiWeaveService = aiWeaveService;
        this.tools.add(conversationLinkTool);
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
                ScenarioBuilder<Void, Void> builder = this.aiWeaveService
                        .getOrCreateScenario(getName());
                builder.setInstructions("You are a helpful assistant.");
                for (var tool : this.tools) {
                    builder.withTool(tool);
                }
                return builder.getOrCreate();
            }
            return existing;
        });
    }
}
