package com.cyster.sage.impl.advisors.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.advisor.Tool;
import com.cyster.sage.impl.advisors.web.WebsiteService.Website;

@Component
public class WebAdvisor implements Advisor<Website> {
    public final String NAME = "websiteBuilder";

    private AdvisorService advisorService;
    private Map<String, Tool<?, Website>> tools = new HashMap<>();    
    private Optional<Advisor<Website>> advisor = Optional.empty();
    
    public WebAdvisor(AdvisorService advisorService,
        WebsiteFileListTool websiteFileListTool,
        WebsiteFileGetTool websiteFileGetTool,
        WebsiteFilePutTool websiteFilePutTool) {
        this.advisorService = advisorService;
        
        this.tools.put(websiteFileListTool.getName(), websiteFileListTool);
        this.tools.put(websiteFileGetTool.getName(), websiteFileGetTool);
        this.tools.put(websiteFilePutTool.getName(), websiteFilePutTool);


    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<Website> createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """ 
You are web developer that has the ability to modify the associated website, by listing, updating or adding files sing your website tools
""";

            AdvisorBuilder<Website> builder = this.advisorService.getOrCreateAdvisor(NAME);
            builder
                .setInstructions(instructions);
                
           for(var tool: tools.values()) {
               builder.withTool(tool);
           }

            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}
