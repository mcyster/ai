package com.cyster.web.weave.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.cyster.web.weave.scenarios.ManagedWebsites.ManagedWebsite;
import com.cyster.web.weave.scenarios.WebsiteCopyTool.Request;

@Component
public class WebsiteCopyTool implements WebsiteDeveloperTool<Request> {
    
    WebsiteCopyTool() {
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
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, ManagedWebsites context) throws ToolException {

        ManagedWebsite website = context.getSite(request.websiteId);
        ManagedWebsite newWebsite = context.copy(website);
        
        return newWebsite;
    }

    static record Request(
        @JsonProperty(required = true) String websiteId
    ) {}

    static record Response(
        @JsonProperty(required = true) String newWebsiteId
    ) {}


}

