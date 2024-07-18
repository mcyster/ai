package com.cyster.weave.impl.scenarios.brand;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
public class BrandScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "Provides details about company brands";
    private AiWeaveService aiWeaveService;
    private Optional<String> brandFetchApiKey;
    private Optional<Scenario<Void, Void>> scenario = Optional.empty();
    

    public BrandScenario(AiWeaveService aiWeaveService,
        @Value("${brandFetchApiKey:#{environment.BRANDFETCH_API_KEY}}") String brandFetchApiKey) {
        this.aiWeaveService = aiWeaveService;
        this.brandFetchApiKey = Optional.of(brandFetchApiKey);
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
            var instructions = """
                You focus on find details on Company brands.
                """;

            AssistantScenarioBuilder<Void, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(getName());
            
            builder.setInstructions("You find details on company brands.")
                .setInstructions(instructions)
                .withTool(new BrandSearchTool(this.brandFetchApiKey))
                .withTool(new BrandFetchTool(this.brandFetchApiKey));
            
            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }
}

