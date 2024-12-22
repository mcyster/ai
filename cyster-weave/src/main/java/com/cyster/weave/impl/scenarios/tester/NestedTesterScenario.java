package com.cyster.weave.impl.scenarios.tester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.ScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
public class NestedTesterScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Helps with testing nested scenarios";

    private AiWeaveService aiWeaveService;
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private final AtomicReference<Scenario<Void, Void>> scenario = new AtomicReference<>();

    public NestedTesterScenario(AiWeaveService aiWeaveService, RandomNumberTool randomNumberTool,
            FailingTesterTool failingTesterTool) {
        this.aiWeaveService = aiWeaveService;
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
