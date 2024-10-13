package com.cyster.web.weave.scenarios;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.web.weave.scenarios.ManagedWebsites.ManagedWebsite;
import com.cyster.web.weave.scenarios.WebsiteFileListTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
class WebsiteFileListTool implements WebsiteDeveloperTool<Request> {

    WebsiteFileListTool() {
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Get a list of filenames associated with the website";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, ManagedWebsites context, OperationLogger operation) throws ToolException {
        ManagedWebsite website;
        try {
            website = context.getSite(request.websiteId());
        } catch (WebsiteException exception) {
            throw new ToolException("Unable to load website: " + request.websiteId, exception);
        }
        return new Response(website.site().getId(), website.site().getAssets());
    }

    static record Request(
        @JsonProperty(required = false)
        String websiteId,
        
        @JsonProperty(required = false)
        @JsonPropertyDescription("Filename glob pattern, defaults to *")
        String filenamePattern
    ) {}

    static record Response(
        String websiteId,
        List<String> filenames
    ) {}

}
