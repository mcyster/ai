package com.cyster.web.weave.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.web.weave.scenarios.ManagedWebsites.ManagedWebsite;
import com.cyster.web.weave.scenarios.WebsiteCopyTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    public Class<ManagedWebsites> getContextClass() {
        return ManagedWebsites.class;
    }

    @Override
    public Object execute(Request request, ManagedWebsites context, OperationLogger operation) throws ToolException {

        ManagedWebsite newWebsite;
        try {
            ManagedWebsite website = context.getSite(request.websiteId);
            newWebsite = context.copy(website);
        } catch (WebsiteException exception) {
            throw new ToolException("Unable to copy website: " + request.websiteId, exception);
        }

        return newWebsite;
    }

    static record Request(@JsonProperty(required = true) String websiteId) {
    }

    static record Response(@JsonProperty(required = true) String newWebsiteId) {
    }

}
