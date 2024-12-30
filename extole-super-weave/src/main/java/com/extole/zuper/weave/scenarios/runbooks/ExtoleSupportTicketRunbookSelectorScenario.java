package com.extole.zuper.weave.scenarios.runbooks;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.tool.SearchTool;
import com.cyster.ai.weave.service.tool.VoidToolAdapter;
import com.cyster.template.StringTemplate;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.runbooks.ExtoleSupportTicketRunbookSelectorScenario.Parameters;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketGetTool;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleSupportTicketRunbookSelectorScenario implements Scenario<Parameters, ExtoleSuperContext> {
    private final String DESCRIPTION = "Find the best Runbook for the specified ticket";

    private SearchTool searchTool;

    private final Advisor<ExtoleSuperContext> advisor;

    public ExtoleSupportTicketRunbookSelectorScenario(AiService aiService, AiAdvisorService aiAdvisorService,
            ExtoleRunbookToolFactory runbookToolFactory, SupportTicketGetTool ticketGetTool,
            ExtoleRunbookDefault defaultRunbook) {

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
                      "step": "Evaluate multiple search results for closest context before defaulting to {{defaultRunbookName}}.",
                      "condition": "Only use as a last resort."
                    }
                    {
                      "step": "Provide your answer in JSON format",
                      "schema": {{{schema}}},
                      "description": [
                        "Its important to always return a json response."
                      ]
                    }
                  ]
                }
                """;

        var schema = aiService.getJsonSchema(Response.class);

        Map<String, String> parameters = new HashMap<>() {
            {
                put("schema", schema);
                put("defaultRunbookName", defaultRunbook.getName());
            }
        };

        String instructions = new StringTemplate(instructionsTemplate).render(parameters);

        System.out.println("!!!!!!!! extole support ticket runbook instructions: " + instructions);

        AdvisorBuilder<ExtoleSuperContext> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());
        builder.setInstructions(instructions);

        this.searchTool = runbookToolFactory.getRunbookSearchTool();
        builder.withTool(new VoidToolAdapter<>(this.searchTool, ExtoleSuperContext.class));
        builder.withTool(ticketGetTool);

        this.advisor = builder.getOrCreate();
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
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ActiveConversationBuilder<ExtoleSuperContext> createConversationBuilder(Parameters parameters,
            ExtoleSuperContext context) {
        if (parameters == null || parameters.ticketNumber() == null || parameters.ticketNumber().isBlank()) {
            throw new IllegalArgumentException("No ticketNumber specified");
        }

        if (!searchTool.isReady()) {
            // Still seem to have to wait a bit sometimes, even when its ready here
            System.out.println("!!!!!!!!!! search tool - vector store not ready !!!");
        }

        return this.advisor.createConversationBuilder(context).addMessage("Ticket: " + parameters.ticketNumber());
    }

    public record Parameters(@JsonProperty(required = true) String ticketNumber) {
    }

    public record Response(@JsonProperty(required = true) String ticketNumber,
            @JsonProperty(required = true) String runbookName, @JsonProperty(required = false) String query,
            @JsonProperty(required = false) String[] searchResults) {
    }
}
