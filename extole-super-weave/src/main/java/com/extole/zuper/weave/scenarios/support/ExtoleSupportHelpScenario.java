package com.extole.zuper.weave.scenarios.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiScenarioService;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioBuilder;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.scheduler.impl.SchedulerTool;
import com.cyster.weave.impl.scenarios.conversation.ConversationLinkTool;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportAdvisorToolLoader;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportTool;

@Component
public class ExtoleSupportHelpScenario implements Scenario<Void, ExtoleSuperContext> {
    private static String DESCRIPTION = "Help with the Extole platform for members of the Extole Support Team";

    private AiScenarioService aiScenarioService;
    private Optional<Scenario<Void, ExtoleSuperContext>> scenario = Optional.empty();
    private Map<String, Tool<?, ?>> tools = new HashMap<>();

    ExtoleSupportHelpScenario(AiScenarioService aiScenarioService, List<ExtoleSupportAdvisorToolLoader> toolLoaders,
            List<ExtoleSupportTool<?>> tools, ConversationLinkTool conversationLinkTool, SchedulerTool schedulerTool) {
        this.aiScenarioService = aiScenarioService;

        for (var tool : tools) {
            this.tools.put(tool.getName(), tool);
        }

        for (var loader : toolLoaders) {
            for (var tool : loader.getTools()) {
                this.tools.put(tool.getName(), tool);
            }
        }
        this.tools.put(schedulerTool.getName(), schedulerTool);
        this.tools.put(conversationLinkTool.getName(), conversationLinkTool);
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
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ConversationBuilder createConversationBuilder(Void parameters, ExtoleSuperContext context) {
        return this.getScenario().createConversationBuilder(parameters, context);
    }

    private Scenario<Void, ExtoleSuperContext> getScenario() {
        if (this.scenario.isEmpty()) {
            String instructions = """
                    You are an senior member of the support team at Extole a SaaS marketing platform.

                    Keep answers brief, and where possible in point form.
                    When referring to a client, use the client short_name.
                    """;

            ScenarioBuilder<Void, ExtoleSuperContext> builder = this.aiScenarioService.getOrCreateScenario(getName());

            builder.setInstructions(instructions);
            for (var tool : tools.values()) {
                builder.withTool(tool);
            }

            this.scenario = Optional.of(builder.getOrCreate());
        }

        return this.scenario.get();
    }
}
