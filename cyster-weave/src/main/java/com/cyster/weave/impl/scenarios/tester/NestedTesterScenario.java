package com.cyster.weave.impl.scenarios.tester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiScenarioService;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioBuilder;
import com.cyster.ai.weave.service.tool.Tool;

@Component
public class NestedTesterScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Helps with testing nested scenarios";

    private AiScenarioService aiScenarioService;
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private final AtomicReference<Scenario<Void, Void>> scenario = new AtomicReference<>();

    public NestedTesterScenario(AiScenarioService aiScenarioService, RandomNumberTool randomNumberTool,
            FailingTesterTool failingTesterTool) {
        this.aiScenarioService = aiScenarioService;
        this.tools.add(randomNumberTool);
        this.tools.add(failingTesterTool);
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
        return scenario.updateAndGet(existing -> {
            if (existing == null) {
                ScenarioBuilder<Void, Void> builder = this.aiScenarioService.getOrCreateScenario(getName());

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
