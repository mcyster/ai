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
import com.extole.weave.advisors.client.ExtoleClientAdvisor;
import com.extole.weave.scenarios.report.ExtoleReportScenario.Parameters;
import com.extole.weave.session.ExtoleSessionContext;


@Component
public class ExtoleReportScenario implements Scenario<Parameters, ExtoleSessionContext> {
    private static final String NAME = "extoleReport";
    private ExtoleClientAdvisor advisor;

    ExtoleReportScenario(ExtoleClientAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Describe an extole report given its report_id";
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
    public Conversation createConversation(Parameters parameters, ExtoleSessionContext context) {
        String systemPrompt = "You are a customer service representative for the Extole SaaS marketing platform. You are looking at the report with id: {{report_id}}";

        MustacheFactory mostacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
        var messageWriter = new StringWriter();
        mustache.execute(messageWriter, parameters);
        messageWriter.flush();

        var advisorContext = new ExtoleClientAdvisor.Context(context.getAccessToken());

        return advisor.createConversation().setOverrideInstructions(messageWriter.toString()).withContext(advisorContext).start();
    }

    public static class Parameters {
        @JsonProperty(required = true)
        public String report_id;
    }

}
