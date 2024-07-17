package com.extole.weave.scenarios.runbooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.scenarios.support.tools.jira.SupportTicketGetTool;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.extole.weave.scenarios.runbooks.ExtoleSupportTicketClientScenario.Parameters;

@Component
public class ExtoleSupportTicketClientScenario implements Scenario<Parameters, Void> {
    public final String NAME = "extoleSupportTicketClient";
    private final String DESCRIPTION = "Find the Extole client id associated for the specified ticket";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Parameters, Void>> scenario = Optional.empty();
    private List<Tool<?, Void>> tools = new ArrayList<>();
    
    public ExtoleSupportTicketClientScenario(AiWeaveService aiWeaveService, ExtoleRunbookToolFactory runbookToolFactory,
            SupportTicketGetTool ticketGetTool) {
        this.aiWeaveService = aiWeaveService;
        this.tools.add(runbookToolFactory.getRunbookSearchTool());
        this.tools.add(ticketGetTool);
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
            String instructions = """
You are an Extole Support Team member handling an incoming ticket. Your task is to identify the Extole client associated with the ticket.

Use the ticketGet to get the specified ticket

If the ticket has a non null clientId use that. 

If the ticket does not have an associated clientId look in the content for a clientId.
In urls its often often in the form client_id=CLIENT_ID

From the search results, choose the Runbook that best fits the ticket's needs.

If no clientId is found use NOT_FOUND

Response: Provide your answer in JSON format, like so:
{
  "ticket_number": "NUMBER",
  "clientId": "CLIENT_ID",
}
""";
           AssistantScenarioBuilder<Parameters, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(NAME);
           builder.setInstructions(instructions);

           for(var tool: tools) {
               builder.withTool(tool);
           }

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }

    public record Parameters(@JsonProperty(required = false) String ticketNumber) {}

}

