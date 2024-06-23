package com.cyster.sage.impl.advisors.web;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.FatalToolException;
import com.cyster.ai.weave.service.advisor.Tool;
import com.cyster.ai.weave.service.advisor.ToolException;
import com.cyster.sage.impl.advisors.web.WebsiteFileGetTool.Request;
import com.cyster.sage.impl.advisors.web.WebsiteService.Website;
import com.cyster.sage.impl.advisors.web.WebsiteService.Website.Asset;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
class WebsiteFileGetTool implements Tool<Request, Website> {
    
    WebsiteFileGetTool() {
    }

    @Override
    public String getName() {
        return "web_developer_file_get";
    }

    @Override
    public String getDescription() {
        return "Read the contents of the specified file";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Website context) throws ToolException {
        
        if (request.filename() == null || request.filename().isBlank()) {
            throw new FatalToolException("No filename specified"); 
        }
              
        Asset asset = context.getAsset(request.filename());
        
        return new Response(asset.filename(), asset.content());
    }

    static record Request(
        @JsonProperty(required = true) String filename 
    ) {}

    static record Response(
        String filename,
        String content 
    ) {}
    
}
