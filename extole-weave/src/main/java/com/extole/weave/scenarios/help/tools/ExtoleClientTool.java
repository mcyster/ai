package com.extole.weave.scenarios.help.tools;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.extole.weave.session.ExtoleSessionContext;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

public class ExtoleClientTool implements Tool<ExtoleClientRequest, ExtoleSessionContext> {

    public ExtoleClientTool() {
    }

    @Override
    public String getName() {
        return "extoleClient";
    }

    @Override
    public String getDescription() {
        return "Gets details about the current client, including client name and client_short_name";
    }

    @Override
    public Class<ExtoleClientRequest> getParameterClass() {
        return ExtoleClientRequest.class;
    }

    @Override
    public Object execute(ExtoleClientRequest request, ExtoleSessionContext context) throws ToolException {
        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setApiKey(context.getAccessToken())
            .build();

        JsonNode resultNode;
        try {
            resultNode = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/clients/" + request.clientId)
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extole_token is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal tool error", exception);
        }

        if (resultNode == null || !resultNode.isObject()) {
            throw new ToolException("Internal tool error, no result");
        }

        return resultNode;
    }

}

class ExtoleClientRequest {
    @JsonPropertyDescription("the client_id associated with the current client")
    @JsonProperty(required = true)
    public String clientId;
}
