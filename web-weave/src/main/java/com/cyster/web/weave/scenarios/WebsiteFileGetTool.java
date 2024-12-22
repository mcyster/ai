package com.cyster.web.weave.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.Weave;
import com.cyster.web.weave.scenarios.ManagedWebsites.ManagedWebsite;
import com.cyster.web.weave.scenarios.WebsiteFileGetTool.Request;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website.Asset;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
class WebsiteFileGetTool implements WebsiteDeveloperTool<Request> {

    WebsiteFileGetTool() {
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
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
    public Class<ManagedWebsites> getContextClass() {
        return ManagedWebsites.class;
    }

    @Override
    public Object execute(Request request, ManagedWebsites context, Weave weave) throws ToolException {

        Asset asset;
        try {
            ManagedWebsite website = context.getSite(request.websiteId);
            asset = website.site().getAsset(request.filename());
        } catch (WebsiteException exception) {
            throw new ToolException("Unable to load website: " + request.websiteId, exception);
        }

        return new Response(request.websiteId, asset.filename(), asset.content());
    }

    static record Request(@JsonProperty(required = true) String websiteId,
            @JsonProperty(required = true) String filename) {
    }

    static record Response(String websiteId, String filename, String content) {
    }

}
