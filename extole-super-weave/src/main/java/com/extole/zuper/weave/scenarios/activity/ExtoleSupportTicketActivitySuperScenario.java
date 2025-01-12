package com.extole.zuper.weave.scenarios.activity;

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
import com.cyster.template.StringTemplate;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.activity.ExtoleSupportTicketActivitySuperScenario.Parameters;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketGetTool;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleSupportTicketActivitySuperScenario implements Scenario<Parameters, ExtoleSuperContext> {
    private final String DEFAULT_ACTIVITY = "unclassified";
    private final String DESCRIPTION = "Find the best Runbook for the specified ticket";

    private final Advisor<ExtoleSuperContext> advisor;
    private SearchTool<ExtoleSuperContext> searchTool;

    public ExtoleSupportTicketActivitySuperScenario(AiService aiService, AiAdvisorService aiAdvisorService,
            ExtoleSupportActivityDocuments activityDocuments, SupportTicketGetTool ticketGetTool) {

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
                      "step": "Issue multiple detailed queries if no Activity is found.",
                      "description": [
                        "Focus on different keywords and combinations from the original prompt."
                      ]
                    },
                    {
                      "step": "Use synonyms or related industry terms if initial queries yield no results."
                    },
                    {
                      "step": "Shorten the original query to 10 words or fewer and try variations.",
                      "condition": "If still no Activity is found."
                    },
                    {
                      "step": "Evaluate multiple search results for closest context before defaulting to '{{defaultActivity}}'.",
                      "condition": "Only use as '{{defaultActivity}}' as a last resort."
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
                put("defaultActivity", DEFAULT_ACTIVITY);
            }
        };

        String instructions = new StringTemplate(instructionsTemplate).render(parameters);

        System.out.println("!!!!!!!! extole support ticket activty instructions: " + instructions);

        AdvisorBuilder<ExtoleSuperContext> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());
        builder.setInstructions(instructions);

        this.searchTool = builder.searchToolBuilder(ExtoleSuperContext.class).withName("activities")
                .withDocumentStore(activityDocuments.getDocuments()).create();
        builder.withTool(searchTool);

        builder.withTool(ticketGetTool);

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

        if (!searchTool.isReady()) {
            // Still seem to have to wait a bit sometimes, even when its ready here
            System.out.println("!!!!!!!!!! search tool - vector store not ready !!!");
        }

        return this.advisor.createConversationBuilder(context).addMessage("Ticket: " + parameters.ticketNumber());
    }

    public record Parameters(@JsonProperty(required = true) String ticketNumber) {
    }

    public record Response(@JsonProperty(required = true) String ticketNumber,
            @JsonProperty(required = true) String activityName, @JsonProperty(required = false) String query,
            @JsonProperty(required = false) String[] searchResults) {
    }
}
