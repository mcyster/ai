package com.cyster.weave.impl.scenarios.webshot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.weave.impl.scenarios.webshot.AssetProvider.AssetId;
import com.cyster.weave.impl.scenarios.webshot.WebshotTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
@ConditionalOnBean(Webshot.class)
class WebshotTool implements Tool<Request, Void> {

    private Webshot webshot;

    WebshotTool(Webshot webshot) {
        this.webshot = webshot;
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
        AssetId assetId = this.webshot.getImage(request.url);
        return new Response(assetId.getId());
    }

    static record Response(@JsonProperty(required = true) String assetId) {
    }

    static record Request(
            @JsonPropertyDescription("Url to web page convert to an image") @JsonProperty(required = true) String url) {
    }

}
