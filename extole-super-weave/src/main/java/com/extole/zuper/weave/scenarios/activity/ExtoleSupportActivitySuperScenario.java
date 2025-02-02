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
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleSupportActivitySuperScenario implements Scenario<Void, Void> {
    private final String DEFAULT_ACTIVITY = "unclassified";
    private final String DESCRIPTION = "Find the best Activity given a set of keywords (intended for testing)";

    private final Advisor<Void> advisor;
    private final SearchTool<Void> searchTool;

    public ExtoleSupportActivitySuperScenario(AiService aiService, AiAdvisorService aiAdvisorService,
            ExtoleSupportActivityDocuments activityDocuments) {

        String instructionsTemplate = """
                {
                  "instructions": [
                    {
                      "step": "Construct a detailed query string based on the prompt",
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

        System.out.println("!!!!!!!! extole suppport activity instructions: " + instructions);

        AdvisorBuilder<Void> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());
        builder.setInstructions(instructions);

        this.searchTool = builder.searchToolBuilder(Void.class).withName("activities")
                .withDocumentStore(activityDocuments.getDocuments()).create();
        builder.withTool(searchTool);

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
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Void parameters, Void context) {

        if (!searchTool.isReady()) {
            // Still seem to have to wait a bit sometimes, even when its ready here
            System.out.println("!!!!!!!!!! search tool - vector store not ready !!!");
        }

        return this.advisor.createConversationBuilder(context);
    }

    public record Response(@JsonProperty(required = true) String activityName,
            @JsonProperty(required = false) String query, @JsonProperty(required = false) String[] searchResults) {
    }
}
