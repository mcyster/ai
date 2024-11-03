package com.extole.weave.scenarios.report;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.template.StringTemplate;
import com.extole.weave.scenarios.help.ExtoleHelpScenario;
import com.extole.weave.scenarios.report.ExtoleReportScenario.Parameters;
import com.extole.weave.session.ExtoleSessionContext;


@Component
public class ExtoleReportScenario implements Scenario<Parameters, ExtoleSessionContext> {
    private static final String DESCRIPTION = "Describe an extole report given its report_id";

    private ExtoleHelpScenario helpScenario;

    ExtoleReportScenario(ExtoleHelpScenario helpScenario) {
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

    public static class Parameters {
        @JsonProperty(required = true)
        public String report_id;
    }

    @Override
    public ConversationBuilder createConversationBuilder(Parameters parameters, ExtoleSessionContext context) {
        String messageTemplate = "You are looking at the report with id: {{report_id}}";

        String message = new StringTemplate(messageTemplate).render(parameters);

        return this.helpScenario.createConversationBuilder(null, context)
            .addMessage(message);
    }

}
