package com.extole.sage.advisors.brand;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.advisor.AdvisorBuilder;
import com.cyster.sherpa.service.advisor.AdvisorService;

@Component
public class ExtoleBrandAdvisor implements Advisor<Void> {
    public final String NAME = "extole-brand";

    private AdvisorService advisorService;
    private Optional<Advisor<Void>> advisor = Optional.empty();
    private Optional<String> brandFetchApiKey;

    public ExtoleBrandAdvisor(AdvisorService advisorService,
        @Value("${brandFetchApiKey:#{environment.BRANDFETCH_API_KEY}}") String brandFetchApiKey) {
        this.advisorService = advisorService;
        this.brandFetchApiKey = Optional.of(brandFetchApiKey);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<Void> createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """ 
You focus on find details on Company brands.
""";
                
            AdvisorBuilder<Void> builder = this.advisorService.getOrCreateAdvisor(NAME);
            
            builder
                .setInstructions(instructions)
                .withTool(new BrandSearchTool(this.brandFetchApiKey))
                .withTool(new BrandFetchTool(this.brandFetchApiKey));
                
             this.advisor = Optional.of(builder.getOrCreate());
        }
        
        return this.advisor.get().createConversation();
    }

}
