package com.extole.weave.scenarios.runbooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
public class ExtoleSupportTicketScenario implements Scenario<RunbookScenarioParameters, Void> {
    public final String NAME = "extoleSupportTicket";
    private final String DESCRIPTION = "Execute the best Runbook for the specified ticket";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<RunbookScenarioParameters, Void>> scenario = Optional.empty();
    private List<Tool<?, Void>> tools = new ArrayList<>();

    public ExtoleSupportTicketScenario(AiWeaveService aiWeaveService, ExtoleTicketRunbookTool runbookTool) {
        this.aiWeaveService = aiWeaveService;
        this.tools.add(runbookTool);
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
    public Class<RunbookScenarioParameters> getParameterClass() {
        return RunbookScenarioParameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public ConversationBuilder createConversationBuilder(RunbookScenarioParameters parameters, Void context) {        
        return this.getScenario().createConversationBuilder(parameters, context)
            .addMessage("Ticket: " + parameters.getTicketNumber());
    }

    private Scenario<RunbookScenarioParameters, Void> getScenario() {
        if (this.scenario.isEmpty()) {
            String defaultInstruction = """
For the given ticket find and execute the Runbook.
Respond with the ticket_number followed by a selected runbook in brackets and then a brief summary of your analysis, i.e:
TICKET_NUMBER (RUNBOOK): SUMMARY
""";
            AssistantScenarioBuilder<RunbookScenarioParameters, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(NAME);
                
            builder.setInstructions(defaultInstruction);
            for(var tool: tools) {
                builder.withTool(tool);
            }

            this.scenario = Optional.of(builder.getOrCreate());       
        }
        return this.scenario.get();
    }
}

