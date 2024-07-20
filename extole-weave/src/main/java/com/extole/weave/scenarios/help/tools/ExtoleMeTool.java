package com.extole.weave.scenarios.help.tools;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.extole.client.web.ExtoleWebClientFactory;
import com.extole.weave.session.ExtoleSessionContext;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.extole.weave.scenarios.help.tools.ExtoleMeTool.Request;

@Component
public class ExtoleMeTool implements Tool<Request, ExtoleSessionContext> {
    private ExtoleWebClientFactory extoleWebClientFactory;

    public ExtoleMeTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extoleMe";
    }

    @Override
    public String getDescription() {
        return "Gets your user_id and associated client_id";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, ExtoleSessionContext context) throws ToolException {
        var webClient = this.extoleWebClientFactory.getWebClient(context.getAccessToken());

        JsonNode resultNode;
        try {
            resultNode = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/me")
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch(WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extole_token is invalid", exception);
        } catch(WebClientException exception) {
            throw new ToolException("Internal tool error", exception);
        }

        if (resultNode == null || !resultNode.isObject()) {
            throw new ToolException(("Internal tool error, no results from request"));
        }

        return resultNode;
    }

    public static class Request {
        @JsonPropertyDescription("Get more detailed information")
        @JsonProperty(required = false)
        public boolean extended;
    }
}


