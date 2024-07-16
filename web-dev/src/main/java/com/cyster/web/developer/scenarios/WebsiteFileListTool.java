package com.cyster.web.developer.scenarios;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.web.developer.scenarios.WebsiteFileListTool.Request;
import com.cyster.web.developer.scenarios.WebsiteService.Website;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
class WebsiteFileListTool implements Tool<Request, Website> {

    WebsiteFileListTool() {
    }

    @Override
    public String getName() {
        return "web_developer_file_list";
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
    public Object execute(Request request, Website context) throws ToolException {
        return new Response(context.getAssets());
    }

    static record Request(
        @JsonProperty(required = false)
        @JsonPropertyDescription("Filename glob pattern, defaults to *")
        String filenamePattern
    ) {}

    static record Response(
        List<String> filenames
    ) {}

}
