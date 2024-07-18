package com.cyster.web.weave.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.web.weave.scenarios.WebsiteFileGetTool.Request;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website.Asset;
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
