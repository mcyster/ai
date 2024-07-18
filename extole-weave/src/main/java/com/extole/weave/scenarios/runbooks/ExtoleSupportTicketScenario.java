package com.extole.weave.scenarios.runbooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.extole.weave.scenarios.runbooks.ExtoleSupportTicketScenario.Parameters;

@Component
public class ExtoleSupportTicketScenario implements Scenario<Parameters, Void> {
    private final String DESCRIPTION = "Execute the best Runbook for the specified ticket";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Parameters, Void>> scenario = Optional.empty();
    private List<Tool<?, Void>> tools = new ArrayList<>();

    public ExtoleSupportTicketScenario(AiWeaveService aiWeaveService,
        ExtoleSupportTicketRunbookSelectorTool runbookSelectorTool,
        ExtoleSupportTicketClientTool ticketClientTool,
        ExtoleSupportTicketRunbookExecuterTool runbookExecuterTool) {
        this.aiWeaveService = aiWeaveService;
        this.tools.add(runbookSelectorTool);
        this.tools.add(ticketClientTool);
        this.tools.add(runbookExecuterTool);
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
        return this.getScenario().createConversationBuilder(parameters, context)
            .addMessage("Ticket: " + parameters.ticketNumber());
    }

    private Scenario<Parameters, Void> getScenario() {
        if (this.scenario.isEmpty()) {
            String defaultInstruction = """
For the given ticket
- find the best runbook
- find the clientId, clientShortName associated with the ticket
- execute the runbook

Respond with the ticket_number followed by a selected runbook in brackets and then a brief summary of your analysis, i.e:
TICKET_NUMBER (RUNBOOK): SUMMARY
""";
            AssistantScenarioBuilder<Parameters, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(getName());
                
            builder.setInstructions(defaultInstruction);
            for(var tool: tools) {
                builder.withTool(tool);
            }

            this.scenario = Optional.of(builder.getOrCreate());       
        }
        return this.scenario.get();
    }
    
    public record Parameters(@JsonProperty(required = true) String ticketNumber) {}

}

