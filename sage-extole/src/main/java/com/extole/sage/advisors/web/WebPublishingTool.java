package com.extole.sage.advisors.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.FatalToolException;
import com.cyster.ai.weave.service.advisor.Tool;
import com.cyster.ai.weave.service.advisor.ToolException;
import com.extole.sage.advisors.web.WebPublishingTool.Request;
import com.extole.sage.advisors.web.WebsiteService.Website;
import com.fasterxml.jackson.annotation.JsonProperty;


@Component
class WebPublishingTool implements Tool<Request, Website> {
    private static final Logger logger = LogManager.getLogger(WebPublishingTool.class);

    private Path directory = Paths.get("/tmp/test");
    
    WebPublishingTool() {
    }

    @Override
    public String getName() {
        return "put_web_file";
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
        
        if (request.filename == null || request.filename.isBlank()) {
            throw new FatalToolException("No filename specified"); 
        }
        if (request.content == null) {
            throw new FatalToolException("No content specified"); 
        }        
        
        context.putAsset(request.filename, request.content);
        
        return context;
    }

    static class Request {
        @JsonProperty(required = true)
        public String filename;

        @JsonProperty(required = true)
        public String content;
    }
    
}
