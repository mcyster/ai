package com.cyster.weave.impl.advisors;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
public class SimpleAssistantScenario implements Scenario<Void, Void> {
    public final String NAME = "simple-assistant";
    public final String DESCRIPTION = "A helpful assistant";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Void, Void>> scenario = Optional.empty();

    public SimpleAssistantScenario(AiWeaveService aiWeaveService) {
      this.aiWeaveService = aiWeaveService;
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
            AssistantScenarioBuilder<Void, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(NAME);
            
            builder.setInstructions("You are a helpful assistant.");
            
            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get().createConversationBuilder(parameters, context);
    }
}
