package com.extole.sage.advisors.client;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorBuilder;
import com.cyster.sherpa.service.advisor.AdvisorService;

@Component
public class ExtoleClientAdvisor implements Advisor<ExtoleClientAdvisor.Context> {
    public final String NAME = "extole-client";

    private AdvisorService advisorService;
    private Optional<Advisor<ExtoleClientAdvisor.Context>> advisor = Optional.empty();
    
    public ExtoleClientAdvisor(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<ExtoleClientAdvisor.Context> createConversation() {
        
        if (this.advisor.isEmpty()) {
            String instructions = """ 
You help with questions around using the Extole SaaS Marketing platform.
""";

            AdvisorBuilder<ExtoleClientAdvisor.Context> builder = 
                this.advisorService.getOrCreateAdvisor(NAME);
            
            builder.setInstructions(instructions);
            
            this.advisor = Optional.of(builder.getOrCreate());
        }
        
        return this.advisor.get().createConversation();
    }

    public static class Context {
        private String userAccessToken; 
        
        Context(String userAccessToken) {
            this.userAccessToken = userAccessToken;
        }
        
        public String etUserAccessToken() {
            return this.userAccessToken;
        }
    }
    
}
