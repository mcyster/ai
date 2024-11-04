package com.extole.zuper.weave.scenarios.runbooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.template.StringTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleRunbookSelectorScenario implements Scenario<Void, Void> {
    
    public final String NAME = "extoleRunbookSelector";
    private final String DESCRIPTION = "Find the best Runbook given a set of keywords (intended for testing)";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Void, Void>> scenario = Optional.empty();
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private String defaultRunbookName;
    
    public ExtoleRunbookSelectorScenario(AiWeaveService aiWeaveService, ExtoleRunbookToolFactory runbookToolFactory,
            ExtoleRunbookDefault defaultRunbook) {
        
        this.aiWeaveService = aiWeaveService;
        this.tools.add(runbookToolFactory.getRunbookSearchTool());
        this.defaultRunbookName = defaultRunbook.getName();
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
      "step": "Construct a detailed query string",
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
            
            var schema = aiWeaveService.getJsonSchema(Response.class);

            Map<String, String> parameters = new HashMap<>() {{
                put("schema", schema);
                put("defaultRunbookName", defaultRunbookName);
            }};
            
            String instructions = new StringTemplate(instructionsTemplate).render(parameters);

            System.out.println("!!!!!!!! extole runbook selector instructions: " + instructions);
            
            AssistantScenarioBuilder<Void, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(getName());
            builder.setInstructions(instructions);

            for(var tool: tools) {
                builder.withTool(tool);
            }

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }
    
    public record Response (
        @JsonProperty(required = true) String runbookName,
        @JsonProperty(required = false) String query,
        @JsonProperty(required = false) String[] searchResults 
    ) {}
}

