package com.extole.weave.scenarios.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.scenarios.support.tools.ExtoleSupportAdvisorToolLoader;
import com.extole.weave.scenarios.support.tools.ExtoleSupportTool;

@Component
public class ExtoleSupportHelpScenario implements Scenario<Void, Void> {
    private static String DESCRIPTION = "Help with the Extole platform for members of the Extole Support Team";
    
    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Void, Void>> scenario = Optional.empty();
    private Map<String, ExtoleSupportTool<?>> tools = new HashMap<>();

    ExtoleSupportHelpScenario(AiWeaveService aiWeaveService, List<ExtoleSupportAdvisorToolLoader> toolLoaders, List<ExtoleSupportTool<?>> tools) {
        this.aiWeaveService = aiWeaveService;
        
        for(var tool: tools) {
            this.tools.put(tool.getName(), tool);
        }

        for(var loader: toolLoaders) {
            for(var tool: loader.getTools()) {
                this.tools.put(tool.getName(), tool);
            }
        }
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
            String instructions = """
You are an senior member of the support team at Extole a SaaS marketing platform.

Keep answers brief, and where possible in point form.
When referring to a client, use the client short_name.
""";

            AssistantScenarioBuilder<Void, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(getName());
            
            builder.setInstructions(instructions);
            for(var tool: tools.values()) {
                builder.withTool(tool);
           }
            
            this.scenario = Optional.of(builder.getOrCreate());
        }
        
        return this.scenario.get();
    }
}
