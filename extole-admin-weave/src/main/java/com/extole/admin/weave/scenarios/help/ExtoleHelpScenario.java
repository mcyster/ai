package com.extole.admin.weave.scenarios.help;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiScenarioService;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioBuilder;
import com.cyster.ai.weave.service.tool.Tool;
import com.extole.admin.weave.scenarios.help.tools.ExtoleClientTimelineTool;
import com.extole.admin.weave.scenarios.help.tools.ExtoleClientTool;
import com.extole.admin.weave.scenarios.help.tools.ExtoleMeTool;
import com.extole.admin.weave.scenarios.help.tools.ExtoleMyAuthorizationsTool;
import com.extole.admin.weave.scenarios.help.tools.ExtoleReportGetTool;
import com.extole.admin.weave.session.ExtoleSessionContext;

@Component
public class ExtoleHelpScenario implements Scenario<Void, ExtoleSessionContext> {
    private final String DESCRIPTION = "Helps using the Extole Platform";

    private AiScenarioService aiScenarioService;
    private Optional<Scenario<Void, ExtoleSessionContext>> scenario = Optional.empty();
    private List<Tool<?, ExtoleSessionContext>> tools = new ArrayList<>();

    ExtoleHelpScenario(AiScenarioService aiScenarioService, ExtoleMeTool extoleMeTool,
            ExtoleClientTool extoleClientTool, ExtoleMyAuthorizationsTool extoleMyAuthorizationsTool,
            ExtoleClientTimelineTool extoleClientTimelineTool, ExtoleReportGetTool extoleReportTool) {
        this.aiScenarioService = aiScenarioService;

        this.tools.add(extoleMeTool);
        this.tools.add(extoleClientTool);
        this.tools.add(extoleMyAuthorizationsTool);
        this.tools.add(extoleClientTimelineTool);
        this.tools.add(extoleReportTool);
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
                    You help with questions around using the Extole SaaS Marketing platform.
                    """;

            ScenarioBuilder<Void, ExtoleSessionContext> builder = this.aiScenarioService.getOrCreateScenario(getName());

            builder.setInstructions(instructions);

            for (var tool : this.tools) {
                builder.withTool(tool);
            }

            this.scenario = Optional.of(builder.getOrCreate());
        }

        return this.scenario.get();
    }
}
