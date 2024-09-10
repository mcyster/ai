package com.extole.weave.scenarios.guides;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.SearchTool;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.scenarios.support.tools.jira.SupportTicketGetTool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.extole.weave.scenarios.guides.ExtoleTicketGuideSelectorScenario.Parameters;

// @Component
public class ExtoleTicketGuideSelectorScenario implements Scenario<Parameters, Void> {
    private final String DESCRIPTION = "Find the best Guide for the specified ticket";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Parameters, Void>> scenario = Optional.empty();
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private SearchTool<Void> searchTool;
    
    public ExtoleTicketGuideSelectorScenario(AiWeaveService aiWeaveService, 
            SupportTicketGetTool ticketGetTool,
    		ExtoleGuideStore extoleGuideStore) {
    	
    	SearchTool<Void> storeSearchTool = extoleGuideStore.createStoreTool();
    	
        this.aiWeaveService = aiWeaveService;
        this.tools.add(storeSearchTool);
        this.tools.add(ticketGetTool);
        this.searchTool = storeSearchTool;
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
        "Remove PII and URLs.",
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
      "step": "Issue multiple detailed queries if no guide is found.",
      "description": [
        "Focus on different keywords and combinations from the original prompt."
      ]
    },
    {
      "step": "Use synonyms or related industry terms if initial queries yield no results."
    },
    {
      "step": "Shorten the original query to 10 words or fewer and try variations.",
      "condition": "If still no guide is found."
    },
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
            }};
            
            MustacheFactory mostacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mostacheFactory.compile(new StringReader(instructionsTemplate), "instructions");
            var messageWriter = new StringWriter();
            mustache.execute(messageWriter, parameters);
            messageWriter.flush();
            var instructions = messageWriter.toString();
    
            System.out.println("!!!!!!!! extole support ticket guies: " + instructions);
            
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
        @JsonProperty(required = true) String guideName,
        @JsonProperty(required = true) String guideLink,
        @JsonProperty(required = false) String query,
        @JsonProperty(required = false) String[] searchResults 
    ) {}
}

