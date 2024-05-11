package com.extole.sage.advisors.client;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.assistant.impl.advisor.FatalToolException;
import com.cyster.assistant.impl.advisor.Tool;
import com.cyster.assistant.impl.advisor.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

class ExtoleClientTimelineTool implements Tool<ExtoleClientTimelineRequest, ExtoleClientAdvisor.Context> {
    
    ExtoleClientTimelineTool() {
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
    public Object execute(ExtoleClientTimelineRequest request, ExtoleClientAdvisor.Context context) throws ToolException {        
        var webClient = ExtoleWebClientBuilder.builder("https://api.extole.io/")
            .setApiKey(context.getUserAccessToken())
            .build();

        JsonNode resultNode = null;
        try {
            resultNode = webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/timeline-entries")
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

