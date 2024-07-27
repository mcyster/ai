package com.extole.weave.scenarios.activity;

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
import com.extole.weave.scenarios.activity.ExtoleSupportTicketActivityScenario.Parameters;

@Component
public class ExtoleSupportTicketActivityScenario implements Scenario<Parameters, Void> {
    private final String DEFAULT_ACTIVITY = "unclassified";
    private final String DESCRIPTION = "Find the best Runbook for the specified ticket";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Parameters, Void>> scenario = Optional.empty();
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private SearchTool<Void> searchTool;
    
    public ExtoleSupportTicketActivityScenario(AiWeaveService aiWeaveService, ExtoleSupportActivityTool supportActivityToolFactory,
            SupportTicketGetTool ticketGetTool) {
        this.aiWeaveService = aiWeaveService;
        this.tools.add(supportActivityToolFactory.getActivityTool());
        this.tools.add(ticketGetTool);
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
{
  "instructions": [
    {
      "step": "Fetch the specified ticket",
    },
    {
      "step": "Construct a detailed query string based on the ticket",
      "description": [
        "Remove PII, company names, and URLs.",
        "Remove duplicate words and common stop words.",
        "Remove special characters and convert all text to lowercase.",
        "Limit the query to 20 words or fewer."
      ]
    },
    {
      "step": "Search using the detailed query."
      "description": [
        "Look for an approximate match, choose the first result."
      ]
    },
    {
      "step": "Issue multiple detailed queries if no Runbook is found.",
      "description": [
        "Focus on different keywords and combinations from the original prompt."
      ]
    },
    {
      "step": "Use synonyms or related industry terms if initial queries yield no results."
    },
    {
      "step": "Shorten the original query to 10 words or fewer and try variations.",
      "condition": "If still no Runbook is found."
    },
    {
      "step": "Evaluate multiple search results for closest context before defaulting to %s.",
      "condition": "Only use as a last resort."
    }
    {
      "step": "Provide your answer in JSON format",
      "schema": %s
    }
  ]
}
""";
    
            var schema = aiWeaveService.getJsonSchema(Response.class);

            System.out.println("!!!!!!!! extole support ticket activity schema:\n" + schema);

            var instructions = String.format(instructionsTemplate, DEFAULT_ACTIVITY, schema);

            System.out.println("!!!!!!!! extole support ticket activty instructions: " + instructions);
            
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
        @JsonProperty(required = true) String activityName,
        @JsonProperty(required = false) String query,
        @JsonProperty(required = false) String[] searchResults 
    ) {}
}

