package com.extole.sage.advisors.web;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.FatalToolException;
import com.cyster.ai.weave.service.advisor.Tool;
import com.cyster.ai.weave.service.advisor.ToolException;
import com.extole.sage.advisors.web.WebPublishingTool.Request;
import com.extole.sage.advisors.web.WebsiteService.Website;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
class WebPublishingTool implements Tool<Request, Website> {
    
    WebPublishingTool() {
    }

    @Override
    public String getName() {
        return "web_file_put";
    }

    @Override
    public String getDescription() {
        return "Write content to the specified file";
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
        if (request.content() == null) {
            throw new FatalToolException("No content specified"); 
        }        
        
        context.putAsset(request.filename(), request.content());
        
        return context;
    }

    static record Request(
        @JsonProperty(required = true) String filename, 
        @JsonProperty(required = true) String content
    ) {}

    
}
