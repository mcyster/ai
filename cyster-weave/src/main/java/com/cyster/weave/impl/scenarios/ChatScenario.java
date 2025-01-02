package com.cyster.weave.impl.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.weave.impl.scenarios.conversation.ConversationLinkTool;

@Component
public class ChatScenario implements Scenario<Void, Void> {
    private final String DESCRIPTION = "A helpful assistant";

    private final Advisor<Void> advisor;

    public ChatScenario(AiAdvisorService advisorService, ConversationLinkTool conversationLinkTool) {
        AdvisorBuilder<Void> builder = advisorService.<Void>getOrCreateAdvisorBuilder(getName())
                .setInstructions("You are a helpful assistant.");

        builder.withTool(conversationLinkTool);

        this.advisor = builder.getOrCreate();
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
    public ActiveConversationBuilder createConversationBuilder(Void parameters, Void context) {
        return this.advisor.createConversationBuilder(context);
    }
}
