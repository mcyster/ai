package com.extole.zuper.weave.scenarios.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import com.cyster.template.StringTemplate;
import com.extole.zuper.weave.scenarios.client.ExtoleSupportTicketClientScenario.Parameters;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleClientGetTool;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleClientSearchTool;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketClientSetTool;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketGetTool;
import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleSupportTicketClientScenario implements Scenario<Parameters, Void> {
    private final String DESCRIPTION = "Find the Extole client associated for the specified ticket";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Parameters, Void>> scenario = Optional.empty();
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private SupportTicketGetTool ticketGetTool;
    private SupportTicketClientSetTool extoleClientSetTool;
    private ExtoleClientGetTool extoleClientGetTool;
    
    public ExtoleSupportTicketClientScenario(AiWeaveService aiWeaveService, 
        SupportTicketGetTool ticketGetTool,
        SupportTicketClientSetTool extoleClientSetTool,
        ExtoleClientGetTool extoleClientGetTool,
        ExtoleClientSearchTool extoleClientSearchTool) {
        this.aiWeaveService = aiWeaveService;
        this.tools.add(ticketGetTool);
        this.tools.add(extoleClientSetTool);
        this.tools.add(extoleClientGetTool);
        this.tools.add(extoleClientSearchTool);
        
        this.ticketGetTool = ticketGetTool;
        this.extoleClientGetTool = extoleClientGetTool;
        this.extoleClientSetTool = extoleClientSetTool;        
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
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public ConversationBuilder createConversationBuilder(Parameters parameters, Void context) {         
        if (parameters == null || parameters.ticketNumber() == null || parameters.ticketNumber().isBlank()) {
            throw new IllegalArgumentException("No ticketNumber specified");
        }
        
        return getScenario().createConversationBuilder(parameters, context)
            .addMessage("Ticket: " + parameters.ticketNumber());
    }
    
    private Scenario<Parameters, Void> getScenario() {        
        if (this.scenario.isEmpty()) {
            String instructionsTemplate = """
You are an Extole Support Team member handling an incoming ticket. Your task is to identify the Extole client associated with the ticket.

Use the {{ticketGetTool}} tool to get the specified ticket.

If the ticket has a non-null clientId, use that. If the ticket does not have an associated clientId, look in the content for a clientId.
URLs often appear in the form client_id=CLIENT_ID.

If you find a client_id, verify the clientId and get the client name and short name using the {{clientGetTool}}.

If you can't find a client_id, load all clients using {{clientSetTool}} and see if you can find a matching name or short name in the ticket.

If a client is found, set the client on the ticket using the {{clientSetTool}} tool.

If no client is found, use NOT_FOUND for the client_id, name, and short_name.

Provide your answer in JSON format as described by this schema:
{{{schema}}}
""";
            
            Map<String, Object> parameters = Map.of(
                    "ticketGetTool", ticketGetTool.getName(),
                    "clientGetTool", extoleClientGetTool.getName(),
                    "clientSetTool", extoleClientSetTool.getName(),
                    "schema", aiWeaveService.getJsonSchema(Response.class)
                );

            String instructions = new StringTemplate(instructionsTemplate).render(parameters);
            
            AssistantScenarioBuilder<Parameters, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(getName());
            builder.setInstructions(instructions);

            for(var tool: tools) {
                builder.withTool(tool);
            }

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }

    public record Parameters(
        @JsonProperty(required = true) String ticketNumber
     ) {}
    
    public record Response (
        @JsonProperty(required = true) String ticketNumber,
        @JsonProperty(required = true) String clientId,
        @JsonProperty(required = true) String clientShortName,
        @JsonProperty(required = true) String clientName
    ) {}
}

