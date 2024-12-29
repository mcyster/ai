package com.extole.admin.weave.scenarios.help.tools;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.FatalToolException;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.ToolException;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.extole.client.web.ExtoleWebClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ExtoleClientTimelineTool implements Tool<ExtoleClientTimelineRequest, ExtoleSessionContext> {
    private ExtoleWebClientFactory extoleWebClientFactory;

    public ExtoleClientTimelineTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extole_client_timeline";
    }

    @Override
    public String getDescription() {
        return "Gets a list of the major events, timelines, that occoured with this client, including client_create and client_launched";
    }

    @Override
    public Class<ExtoleClientTimelineRequest> getParameterClass() {
        return ExtoleClientTimelineRequest.class;
    }

    @Override
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public Object execute(ExtoleClientTimelineRequest request, ExtoleSessionContext context, Weave weave)
            throws ToolException {
        var webClient = this.extoleWebClientFactory.getWebClient(context.getAccessToken());

        JsonNode resultNode = null;
        try {
            resultNode = webClient.get().uri(uriBuilder -> uriBuilder.path("/v2/timeline-entries").build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();

        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extole_token is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal tool error", exception);
        }

        if (resultNode == null || !resultNode.isArray()) {
            throw new ToolException(("Internal tool error, no results from request"));
        }

        return resultNode;
    }

}

class ExtoleClientTimelineRequest {
    @JsonPropertyDescription("filters the timeline by the specified tags")
    @JsonProperty(required = false)
    public String tags;
}
