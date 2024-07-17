package com.extole.weave.scenarios.report;

import java.io.StringReader;
import java.io.StringWriter;


import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.scenarios.help.ExtoleHelpScenario;
import com.extole.weave.scenarios.report.ExtoleReportScenario.Parameters;
import com.extole.weave.session.ExtoleSessionContext;


@Component
public class ExtoleReportScenario implements Scenario<Parameters, ExtoleSessionContext> {
    public static final String NAME = "extoleReport";
    private static final String DESCRIPTION = "Describe an extole report given its report_id";

    private ExtoleHelpScenario helpScenario;

    ExtoleReportScenario(ExtoleHelpScenario helpScenario) {
        this.helpScenario = helpScenario;
    }

    @Override
    public String getName() {
        return NAME;
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
        String message = "You are looking at the report with id: {{report_id}}";

        MustacheFactory mostacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mostacheFactory.compile(new StringReader(message), "message");
        var messageWriter = new StringWriter();
        mustache.execute(messageWriter, parameters);
        messageWriter.flush();

        return this.helpScenario.createConversationBuilder(null, context)
            .addMessage(messageWriter.toString());
    }

}
