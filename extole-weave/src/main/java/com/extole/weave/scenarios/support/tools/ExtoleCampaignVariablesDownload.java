package com.extole.weave.scenarios.support.tools;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.extole.weave.scenarios.support.tools.ExtoleCampaignVariablesGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class ExtoleCampaignVariablesGetTool implements ExtoleSupportTool<Request> {
    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtoleCampaignVariablesGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extoleCampaignVariablesGet";
    }

    @Override
    public String getDescription() {
        return "Get the variables associated with a campaign by campaignId";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        JsonNode result;

        try {
            result = this.extoleWebClientFactory.getWebClient(request.clientId).get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/campaigns/" + request.campaignId + "/creative/variable/batch/values.json")
                    .queryParam("type", "TEXT")
                    .queryParam("tags", "TRANSLATABLE")
                    .queryParam("zone_state", "enabled")
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException.Forbidden exception) {
            var errorResponse = exception.getResponseBodyAs(JsonNode.class);
            if (errorResponse.has("code") && errorResponse.path("code").asText().equals("campaign_not_found")) {
                throw new ToolException("CampaignId not found");
            }
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get campaignid");
        }

        if (!result.isArray()) {
            throw new ToolException("Fetch failed unexpected result");
        }

        return result;
    }

    static class Request {
        @JsonPropertyDescription("The 1 to 12 digit id for a client.")
        @JsonProperty(required = true)
        public String clientId;

        @JsonPropertyDescription("The 20 to 25 digit id for a campaign.")
        @JsonProperty(required = true)
        public String campaignId;
    }
}
