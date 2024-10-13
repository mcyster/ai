package com.cyster.web.weave.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.web.weave.scenarios.ManagedWebsites.ManagedWebsite;
import com.cyster.web.weave.scenarios.WebsiteFilePutTool.Request;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website.Asset;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
class WebsiteFilePutTool implements WebsiteDeveloperTool<Request> {
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
    public Object execute(Request request, ManagedWebsites context, OperationLogger operation) throws ToolException {

        ManagedWebsite website;
        try {
            website = context.getSite(request.websiteId);
        } catch (WebsiteException exception) {
            throw new ToolException("Unable to load website: " + request.websiteId, exception);
        }
        
        if (request.filename() == "index.html" && !request.content().contains(CHAT_INCLUDE)) {
            throw new FatalToolException("Do not remove the script tag: " + CHAT_INCLUDE);
        }
        
        Asset asset = website.site().putAsset(request.filename(), request.content());

        return new Response(website.site().getId(), asset.filename(), asset.content());
    }

    static record Request(
        @JsonProperty(required = true) String websiteId,
        @JsonProperty(required = true) String filename,
        @JsonProperty(required = true) String content
    ) {}

    static record Response(
        @JsonProperty(required = true) String websiteId,
        @JsonProperty(required = true) String filename,
        @JsonProperty(required = true) String content
    ) {}

}
