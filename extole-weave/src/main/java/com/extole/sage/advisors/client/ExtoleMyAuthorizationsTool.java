package com.extole.sage.advisors.client;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.advisor.FatalToolException;
import com.cyster.ai.weave.service.advisor.Tool;
import com.cyster.ai.weave.service.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

class ExtoleMyAuthorizationsTool implements Tool<MyAuthorizationsRequest, ExtoleClientAdvisor.Context> {
    
    ExtoleMyAuthorizationsTool() {
    }

    @Override
    public String getName() {
        return "extoleMeAuthorizations";
    }

    @Override
    public String getDescription() {
        return "Describes your authorization / scopes / access level.";
    }

    @Override
    public Class<MyAuthorizationsRequest> getParameterClass() {
        return MyAuthorizationsRequest.class;
    }

    @Override
    public Object execute(MyAuthorizationsRequest request, ExtoleClientAdvisor.Context context) throws ToolException {     
        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setApiKey(context.getUserAccessToken())
            .build();

        JsonNode resultNode;
        try {
            resultNode = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/tokens")
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

}

class MyAuthorizationsRequest {
    @JsonPropertyDescription("Get more detailed information")
    @JsonProperty(required = false)
    public boolean extended;
}
