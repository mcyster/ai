package com.cyster.weave.impl.scenarios.tester;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
public class TesterScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Helps with testing scenarios";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Void, Void>> scenario = Optional.empty();
    private List<Tool<?,Void>> tools = new ArrayList<>();
    
    public TesterScenario(AiWeaveService aiWeaveService, 
            RandomNumberTool randomNumberTool,
            FailingTesterTool failingTesterTool,
            NestedAiTool nestedAiTool) {
      this.aiWeaveService = aiWeaveService;
      this.tools.add(randomNumberTool);
      this.tools.add(failingTesterTool);
      this.tools.add(nestedAiTool);
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
        return this.getScenario().createConversationBuilder(parameters, context);
    }
    
    private Scenario<Void, Void> getScenario() {
        if (this.scenario.isEmpty()) {
            AssistantScenarioBuilder<Void, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(getName());
            
            builder.setInstructions("You are a helpful assistant.");
            for(var tool: this.tools) {
                builder.withTool(tool);
            }
            
            this.scenario = Optional.of(builder.getOrCreate());
        }
        
        return this.scenario.get();
    }
}

