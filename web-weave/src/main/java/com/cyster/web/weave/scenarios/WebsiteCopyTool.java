package com.cyster.web.weave.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website;

@Component
public class WebsiteCopyTool implements Tool<Void, Website> {
    private WebsiteProvider websiteProvider;
    
    WebsiteCopyTool(WebsiteProvider websiteProvider) {
        this.websiteProvider = websiteProvider;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Make a copy of the website";
    }

    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Object execute(Void request, Website context) throws ToolException {

        Website newWebsite = websiteProvider.copy(context);
        
        return newWebsite;
    }


}

