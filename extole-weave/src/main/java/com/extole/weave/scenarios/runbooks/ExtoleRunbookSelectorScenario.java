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
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleRunbookSelectorScenario implements Scenario<Void, Void> {
    public final String NAME = "extoleRunbookSelector";
    private final String DESCRIPTION = "Find the best Runbook given a set of keywords (intended for testing)";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Void, Void>> scenario = Optional.empty();
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private String defaultRunbookName;
    private SearchTool<Void> searchTool;
    
    public ExtoleRunbookSelectorScenario(AiWeaveService aiWeaveService, ExtoleRunbookToolFactory runbookToolFactory,
            ExtoleRunbookDefault defaultRunbook) {
        this.aiWeaveService = aiWeaveService;
        this.tools.add(runbookToolFactory.getRunbookSearchTool());
        this.defaultRunbookName = defaultRunbook.getName();
        this.searchTool = runbookToolFactory.getRunbookSearchTool();
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
You are an Extole Support Team member handling an incoming ticket. Your task is to identify the most appropriate Runbook from a prompt.

Construct a query string from the prompt.
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
            
            AssistantScenarioBuilder<Void, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(NAME);
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

