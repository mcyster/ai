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
    public final String NAME = "brand";
    private final String DESCRIPTION = "Provides details about company brands";
    private AiWeaveService aiWeaveService;
    private Optional<String> brandFetchApiKey;
    private Optional<Scenario<Void, Void>> brandScenario = Optional.empty();
    

    public BrandScenario(AiWeaveService aiWeaveService,
        @Value("${brandFetchApiKey:#{environment.BRANDFETCH_API_KEY}}") String brandFetchApiKey) {
        this.aiWeaveService = aiWeaveService;
        this.brandFetchApiKey = Optional.of(brandFetchApiKey);
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
        if (this.brandScenario.isEmpty()) {
            var instructions = """
                You focus on find details on Company brands.
                """;

            AssistantScenarioBuilder<Void, Void> builder = this.aiWeaveService.getOrCreateAssistantScenario(NAME);
            
            builder.setInstructions("You find details on company brands.")
                .setInstructions(instructions)
                .withTool(new BrandSearchTool(this.brandFetchApiKey))
                .withTool(new BrandFetchTool(this.brandFetchApiKey));
            
            this.brandScenario = Optional.of(builder.getOrCreate());
        }
        
        return this.brandScenario.get().createConversationBuilder(parameters, context);
    }

}
