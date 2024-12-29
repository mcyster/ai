package com.extole.zuper.weave.scenarios.activity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.ScenarioBuilder;
import com.cyster.ai.weave.service.SearchTool;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.template.StringTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleSupportActivityScenario implements Scenario<Void, Void> {
    private final String DEFAULT_ACTIVITY = "unclassified";
    private final String DESCRIPTION = "Find the best Activity given a set of keywords (intended for testing)";

    private AiService aiWeaveService;
    private Optional<Scenario<Void, Void>> scenario = Optional.empty();
    private SearchTool searchTool;

    public ExtoleSupportActivityScenario(AiService aiWeaveService, ExtoleSupportActivityTool activityTool) {
        this.aiWeaveService = aiWeaveService;
        this.searchTool = activityTool.getActivityTool();
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
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public ConversationBuilder createConversationBuilder(Void parameters, Void context) {
        return getScenario().createConversationBuilder(parameters, context);
    }

    private Scenario<Void, Void> getScenario() {
        if (this.scenario.isEmpty()) {
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

            var schema = aiWeaveService.getJsonSchema(Response.class);

            Map<String, String> parameters = new HashMap<>() {
                {
                    put("schema", schema);
                    put("defaultActivity", DEFAULT_ACTIVITY);
                }
            };

            String instructions = new StringTemplate(instructionsTemplate).render(parameters);

            System.out.println("!!!!!!!! extole suppport activity instructions: " + instructions);

            ScenarioBuilder<Void, Void> builder = this.aiWeaveService.getOrCreateScenario(getName());
            builder.setInstructions(instructions);
            builder.withTool(searchTool);

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }

    public record Response(@JsonProperty(required = true) String activityName,
            @JsonProperty(required = false) String query, @JsonProperty(required = false) String[] searchResults) {
    }
}
