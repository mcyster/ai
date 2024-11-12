package com.extole.admin.weave.scenarios.report;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.template.StringTemplate;
import com.extole.admin.weave.scenarios.help.ExtoleHelpScenario;
import com.extole.admin.weave.scenarios.report.ExtoleReportDescriptionScenario.Parameters;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleReportDescriptionScenario implements Scenario<Parameters, ExtoleSessionContext> {
    private static final String DESCRIPTION = "Describe an extole report given its report_id";

    private ExtoleHelpScenario helpScenario;

    ExtoleReportDescriptionScenario(ExtoleHelpScenario helpScenario) {
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

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public ConversationBuilder createConversationBuilder(Parameters parameters, ExtoleSessionContext context) {
        String messageTemplate = """
                You are looking at the report with id: {{reportId}}

                Provide a brief user friendly business level description of the report.
                Do not mention the executory type, format or time range of the report as thse are already visible in the UI.
                """;

        String message = new StringTemplate(messageTemplate).render(parameters);

        return this.helpScenario.createConversationBuilder(null, context).addMessage(message);
    }

    public static record Parameters(@JsonProperty(required = true) String reportId) {
    }
}
