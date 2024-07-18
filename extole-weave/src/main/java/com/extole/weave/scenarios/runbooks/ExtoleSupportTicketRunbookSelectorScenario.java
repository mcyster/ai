package com.extole.weave.scenarios.runbooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.SearchTool;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.scenarios.support.tools.jira.SupportTicketGetTool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.extole.weave.scenarios.runbooks.ExtoleSupportTicketRunbookSelectorScenario.Parameters;

@Component
public class ExtoleSupportTicketRunbookSelectorScenario implements Scenario<Parameters, Void> {
    public final String NAME = "extoleSupportTicketRunbookSelector";
    private final String DESCRIPTION = "Find the best Runbook for the specified ticket";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Parameters, Void>> scenario = Optional.empty();
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private String defaultRunbookName;
    private SearchTool<Void> searchTool;
    
    public ExtoleSupportTicketRunbookSelectorScenario(AiWeaveService aiWeaveService, ExtoleRunbookToolFactory runbookToolFactory,
            SupportTicketGetTool ticketGetTool,
            ExtoleRunbookDefault defaultRunbook) {
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

        if (!searchTool.isReady()) {
            // Still seem to have to wait a bit sometimes, even when its ready here
            System.out.println("!!!!!!!!!! search tool - vector store not ready !!!");
        }
        
        return getScenario().createConversationBuilder(parameters, context)
            .addMessage("Ticket: " + parameters.ticketNumber());
    }
    
    private Scenario<Parameters, Void> getScenario() {
        if (this.scenario.isEmpty()) {
            String instructionsTemplate = """
You are an Extole Support Team member handling an incoming ticket. Your task is to identify the most appropriate Runbook for resolving the ticket's issue.

Ticket Information: Use the ticketGet to get the specified ticket

Construct a query string from the ticket description.
- Remove any personally identifiable information (PII).
- Remove any company names.
- Remove any URLs.
- Remove duplicate words. 
- Remove common stop words (e.g., "this", "is", "in").
- Remove special characters
- Convert all text to lowercase.
- Limit the query to 20 words or fewer.

Search in your vector store with the prepared query.

We are not looking for an exact match!

Take the first result, that is the selected Runbook.

If no Runbook is found, use the default "%s" Runbook as a last resort.

If you have choosen the default Runbook, limit the query to 5 words and try again.

Provide your answer in JSON format as describe by this schema:
%s
""";
    
            var schema = aiWeaveService.getJsonSchema(Response.class);

            System.out.println("!!!!!!!! extole support ticket schema:\n" + schema);

            var instructions = String.format(instructionsTemplate, defaultRunbookName, schema);

            System.out.println("!!!!!!!! extole support ticket instructions: " + instructions);
            
            AssistantScenarioBuilder<Parameters, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(NAME);
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
        @JsonProperty(required = true) String runbookName,
        @JsonProperty(required = false) String query,
        @JsonProperty(required = false) String[] searchResults 
    ) {}
}

