package com.extole.weave.scenarios.runbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.scenarios.runbooks.ExtoleRunbookOther;

@Component
public class ExtoleRunbookSelectorScenarioOld implements Scenario<Void, Void> {
    public final String NAME = "extoleRunbookSelector";
    private final String DESCRIPTION = "Find the best Runbook for the current prompt context";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Void, Void>> scenario = Optional.empty();
    
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private String defaultRunbookName;

    public ExtoleRunbookSelectorScenarioOld(AiWeaveService aiWeaveService,
            ExtoleRunbookToolFactory runbookToolFactory,
            ExtoleRunbookOther defaultRunbook) {
        this.aiWeaveService = aiWeaveService;
        this.defaultRunbookName = defaultRunbook.getName();

        tools.add(runbookToolFactory.getRunbookSearchTool());
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
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Void parameters, Void context) {
        throw new UnsupportedOperationException("Method is deprectated and being removed from interface");
    }

    @Override
    public ConversationBuilder createConversationBuilder(Void parameters, Void context) {
        if (this.scenario.isEmpty()) {
            String instructions = """
Interpret the prompt as keywords and use it to create a query
- fix any grammar
- remove duplicate words
- remove PII, URLs, company names
- remove stop words (common words like \"this\", \"is\", \"in\", \"by\", \"with\" etc),
- normalize the text (convert to lower case and remove special characters)
- keep to 20 words or less.

Search for the best runbook.

Respond in json in the following form { "runbook": "RUNBOOK_NAME" }
""";

            AssistantScenarioBuilder<Void, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(NAME);
            
            builder.setInstructions(String.format(instructions, this.defaultRunbookName));
            for(var tool: tools) {
                builder.withTool(tool);
            }

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get().createConversationBuilder(parameters, context);
    }


}

