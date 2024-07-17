package com.extole.weave.scenarios.runbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.SearchTool;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.scenarios.runbooks.ExtoleRunbookOther;
import com.extole.weave.scenarios.runbooks.RunbookScenarioParameters;
import com.extole.weave.scenarios.support.tools.jira.SupportTicketGetTool;

@Component
public class ExtoleSupportTicketRunbookScenario implements Scenario<RunbookScenarioParameters, Void> {
    public final String NAME = "extoleSupportTicketRunbook";
    private final String DESCRIPTION = "Find the best Runbook for the specified ticket";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<RunbookScenarioParameters, Void>> scenario = Optional.empty();
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private String defaultRunbookName;
    private SearchTool<Void> searchTool;
    
    public ExtoleSupportTicketRunbookScenario(AiWeaveService aiWeaveService, ExtoleRunbookToolFactory runbookToolFactory,
            SupportTicketGetTool ticketGetTool,
            ExtoleRunbookOther defaultRunbook) {
        this.aiWeaveService = aiWeaveService;
        this.tools.add(runbookToolFactory.getRunbookSearchTool());
        this.tools.add(ticketGetTool);
        this.defaultRunbookName = defaultRunbook.getName();
        this.searchTool = runbookToolFactory.getRunbookSearchTool();
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
        if (parameters == null || parameters.getTicketNumber() == null || parameters.getTicketNumber().isBlank()) {
            throw new IllegalArgumentException("No ticketNumber specified");
        }

        if (!searchTool.isReady()) {
            // Still seem to have to wait a bit sometimes, even when its ready here
            System.out.println("!!!!!!!!!! search tool - vector store not ready !!!");
        }
        
        return getScenario().createConversationBuilder(parameters, context)
            .addMessage("Ticket: " + parameters.getTicketNumber());
    }
    
    private Scenario<RunbookScenarioParameters, Void> getScenario() {
        if (this.scenario.isEmpty()) {
            String instructions = """
You are an Extole Support Team member handling an incoming ticket. Your task is to identify the most appropriate Runbook for resolving the ticket's issue.

Ticket Information: Use the ticketGet to get the specified ticket

Query Preparation:

Construct a query string using the ticket's classification, title, and description.
Edit the query to:
- Correct grammar mistakes.
- Eliminate duplicate words and personal identifiable information (PII), URLs, and company names.
- Remove common stop words (e.g., "this", "is", "in").
- Convert all text to lowercase and remove special characters.
- Limit the query to 20 words or fewer.

Runbook Search: Search in your vector store with your prepared query.

Runbook Selection:

From the search results, choose the Runbook that best fits the ticket's needs.
If no suitable Runbook is found, use the default "%s" Runbook.
Response: Provide your answer in JSON format, like so:
{
  "ticket_number": "NUMBER",
  "runbook": "RUNBOOK_NAME",
  "query": "QUERY"
}
""";
           AssistantScenarioBuilder<RunbookScenarioParameters, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(NAME);
           builder.setInstructions(String.format(instructions, this.defaultRunbookName));

           for(var tool: tools) {
               builder.withTool(tool);
           }

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }

}

