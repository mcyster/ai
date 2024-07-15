package com.cyster.sage.impl.advisors;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.AdvisorService;

// Currently a Scenario creates an Conversation, should create an Assistant, then this would be used
// an Assistant would return a Conversation

@Component
public class CodingAdvisor implements Advisor<Void> {
    public final String NAME = "code-advisor";

    private AdvisorService advisorService;
    private Optional<Advisor<Void>> advisor = Optional.empty();
    
    public CodingAdvisor(AdvisorService advisorService) {
      this.advisorService = advisorService;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<Void> createConversation() {
        if (this.advisor.isEmpty()) {
            AdvisorBuilder<Void> builder = this.advisorService.getOrCreateAdvisor(NAME);
            builder
                .setInstructions("You are a highly experienced software engineer. You focus on creating simple, highly readable software");
                // .withTool()
                
            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}
