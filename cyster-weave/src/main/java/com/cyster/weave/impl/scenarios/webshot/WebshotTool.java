package com.cyster.weave.impl.scenarios.webshot;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;
import com.cyster.weave.impl.scenarios.webshot.WebshotTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
@ConditionalOnBean(WebshotService.class)
public class WebshotTool implements Tool<Request, Void> {

    private WebshotService webshotService;

    WebshotTool(@Qualifier("webshotServiceImpl") WebshotService webshot) {
        this.webshotService = webshot;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Take a screenshot of a web page";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Object execute(Request request, Void context, Weave weave) {
        String name = request.name;
        if (name == null || name.isBlank()) {
            name = UUID.randomUUID().toString();
        }

        AccessibleAsset accessibleAsset = this.webshotService.takeSnapshot(name, request.url);

        return new Response(accessibleAsset.assetName().name(), "png", accessibleAsset.assetUri().toString());
    }

    static record Response(@JsonProperty(required = true) String assetName, String assetType, String assetUrl) {
    }

    static record Request(
            @JsonPropertyDescription("Short descriptive name for the image") @JsonProperty(required = false) String name,
            @JsonPropertyDescription("Url to web page convert to an image") @JsonProperty(required = true) String url) {
    }

}
