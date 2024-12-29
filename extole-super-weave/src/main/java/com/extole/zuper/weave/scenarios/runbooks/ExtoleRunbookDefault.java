package com.extole.zuper.weave.scenarios.runbooks;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.template.StringTemplate;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.support.ExtoleSupportHelpScenario;

@Component
public class ExtoleRunbookDefault implements RunbookScenario {
    private static String DESCRIPTION = "Analyzes and comments on tickets that could not be classfied more specifically";
    private static String KEYWORDS = "nothing";

    private static String INSTRUCTIONS_TEMPLATE = """
            Load the support ticket {{ticket_number}}

            Note the ticket number, and note its classified as "other".
            """;

    private ExtoleSupportHelpScenario helpScenario;

    ExtoleRunbookDefault(ExtoleSupportHelpScenario helpScenario) {
        this.helpScenario = helpScenario;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Scenario", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getKeywords() {
        return KEYWORDS;
    }

    @Override
    public Class<RunbookScenarioParameters> getParameterClass() {
        return RunbookScenarioParameters.class;
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ActiveConversationBuilder<ExtoleSuperContext> createConversationBuilder(RunbookScenarioParameters parameters,
            ExtoleSuperContext context) {
        String instructions = new StringTemplate(INSTRUCTIONS_TEMPLATE).render(parameters);

        return this.helpScenario.createConversationBuilder(null, context).setOverrideInstructions(instructions);
    }
}
