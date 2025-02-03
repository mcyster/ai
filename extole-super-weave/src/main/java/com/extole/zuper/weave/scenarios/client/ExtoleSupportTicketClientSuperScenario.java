package com.extole.zuper.weave.scenarios.client;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.template.StringTemplate;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.client.ExtoleSupportTicketClientSuperScenario.Parameters;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleClientGetTool;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleClientSearchTool;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketClientSetTool;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketGetTool;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleSupportTicketClientSuperScenario implements Scenario<Parameters, ExtoleSuperContext> {
    private final String DESCRIPTION = "Find the Extole client associated for the specified ticket";

    private final Advisor<ExtoleSuperContext> advisor;

    public ExtoleSupportTicketClientSuperScenario(AiService aiService, AiAdvisorService aiAdvisorService,
            SupportTicketGetTool ticketGetTool, SupportTicketClientSetTool extoleClientSetTool,
            ExtoleClientGetTool extoleClientGetTool, ExtoleClientSearchTool extoleClientSearchTool) {

        String instructionsTemplate = """
                You are an Extole Support Team member handling an incoming ticket. Your task is to identify the Extole client associated with the ticket.

                Use the {{ticketGetTool}} tool to get the specified ticket.

                If the ticket has a non-null clientId, use that. If the ticket does not have an associated clientId, look in the content for a clientId.
                URLs often appear in the form client_id=CLIENT_ID.

                If you find a client_id, verify the clientId and get the client name and short name using the {{clientGetTool}}.

                If you can't find a client_id, load all clients using {{clientSearchTool}} and see if you can find a matching name or short name in the ticket.

                If a client is found, set the client on the ticket using the {{clientSetTool}} tool.

                If no client is found, use NOT_FOUND for the client_id, name, and short_name.

                Provide your answer in JSON format as described by this schema:
                {{{schema}}}
                """;

        Map<String, Object> parameters = Map.of("ticketGetTool", ticketGetTool.getName(), "clientGetTool",
                extoleClientGetTool.getName(), "clientSearchTool", extoleClientSearchTool.getName(), "clientSetTool",
                extoleClientSetTool.getName(), "schema", aiService.getJsonSchema(Response.class));

        String instructions = new StringTemplate(instructionsTemplate).render(parameters);

        AdvisorBuilder<ExtoleSuperContext> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());
        builder.setInstructions(instructions);

        builder.withTool(ticketGetTool);
        builder.withTool(extoleClientSetTool);
        builder.withTool(extoleClientGetTool);
        builder.withTool(extoleClientSearchTool);

        this.advisor = builder.getOrCreate();

    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("SuperScenario", "");
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
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Parameters parameters, ExtoleSuperContext context) {
        if (parameters == null || parameters.ticketNumber() == null || parameters.ticketNumber().isBlank()) {
            throw new IllegalArgumentException("No ticketNumber specified");
        }

        return this.advisor.createConversationBuilder(context).addMessage("Ticket: " + parameters.ticketNumber());
    }

    public record Parameters(@JsonProperty(required = true) String ticketNumber) {
    }

    public record Response(@JsonProperty(required = true) String ticketNumber,
            @JsonProperty(required = true) String clientId, @JsonProperty(required = true) String clientShortName,
            @JsonProperty(required = true) String clientName) {
    }
}
