package com.cyster.web.weave.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.web.weave.scenarios.WebsiteFilePutTool.Request;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
class WebsiteFilePutTool implements Tool<Request, Website> {
    private static final String CHAT_INCLUDE = "<script src=\"/sites/managed/chat/chat.js\" data-scenario=\"WebDeveloper\" data-href-site-name=\"/([^/]+)/[^/]+$\"></script>";
    
    WebsiteFilePutTool() {
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Create or update the specified filename with the specified content";
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

        if (!request.content().contains(CHAT_INCLUDE)) {
            throw new FatalToolException("Do not remove the script tag: " + CHAT_INCLUDE);
        }
        context.putAsset(request.filename(), request.content());

        return context;
    }

    static record Request(
        @JsonProperty(required = true) String filename,
        @JsonProperty(required = true) String content
    ) {}


}
