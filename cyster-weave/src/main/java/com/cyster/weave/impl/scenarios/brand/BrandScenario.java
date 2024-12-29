package com.cyster.weave.impl.scenarios.brand;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.ScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
@ConditionalOnBean(BrandFetchTool.class)
public class BrandScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Provides details about company brands";
    private AiService aiWeaveService;
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private final AtomicReference<Scenario<Void, Void>> scenario = new AtomicReference<>();

    public BrandScenario(AiService aiWeaveService, BrandFetchTool brandFetchTool,
            BrandSearchTool brandSearchTool) {
        this.aiWeaveService = aiWeaveService;
        this.tools.add(brandFetchTool);
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
                        You focus on find details on Company brands.
                        """;

                ScenarioBuilder<Void, Void> builder = this.aiWeaveService
                        .getOrCreateScenario(getName());

                builder.setInstructions(instructions);
                for (var tool : this.tools) {
                    builder.withTool(tool);
                }

                return builder.getOrCreate();
            }
            return existing;
        });
    }
}
