package com.extole.admin.weave.scenarios.help;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.admin.weave.scenarios.help.tools.ExtoleClientTimelineTool;
import com.extole.admin.weave.scenarios.help.tools.ExtoleClientTool;
import com.extole.admin.weave.scenarios.help.tools.ExtoleMeTool;
import com.extole.admin.weave.scenarios.help.tools.ExtoleMyAuthorizationsTool;
import com.extole.admin.weave.scenarios.help.tools.ExtoleReportGetTool;
import com.extole.admin.weave.session.ExtoleSessionContext;

@Component
public class ExtoleHelpScenario implements Scenario<Void, ExtoleSessionContext> {
    private final String DESCRIPTION = "Helps using the Extole Platform";

    private final Advisor<ExtoleSessionContext> advisor;

    ExtoleHelpScenario(AiAdvisorService aiAdvisorService, ExtoleMeTool extoleMeTool, ExtoleClientTool extoleClientTool,
            ExtoleMyAuthorizationsTool extoleMyAuthorizationsTool, ExtoleClientTimelineTool extoleClientTimelineTool,
            ExtoleReportGetTool extoleReportTool) {

        String instructions = """
                You help with questions around using the Extole SaaS Marketing platform.
                """;

        AdvisorBuilder<ExtoleSessionContext> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(instructions);

        builder.withTool(extoleMeTool);
        builder.withTool(extoleClientTool);
        builder.withTool(extoleMyAuthorizationsTool);
        builder.withTool(extoleClientTimelineTool);
        builder.withTool(extoleReportTool);

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
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public ActiveConversationBuilder<ExtoleSessionContext> createConversationBuilder(Void parameters,
            ExtoleSessionContext context) {
        return this.advisor.createConversationBuilder(context);
    }
}
