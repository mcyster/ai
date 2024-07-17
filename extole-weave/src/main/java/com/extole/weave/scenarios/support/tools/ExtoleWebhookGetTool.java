package com.extole.weave.scenarios.support.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.extole.weave.scenarios.support.tools.ExtoleWebhookGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class ExtoleWebhookGetTool implements ExtoleSupportTool<Request> {
    private static final Logger logger = LogManager.getLogger(ExtoleWebClientFactory.class);

    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtoleWebhookGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extole_webhook_get";
    }

    @Override
    public String getDescription() {
        return "Loads the webhook configuration by webhook_id";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        JsonNode result;
        try {
            result = this.extoleWebClientFactory.getWebClient(request.client_id).get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v6/webhooks/" + request.webhook_id)
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get clients");
        }

        logger.trace("webhook.fetch result: " + result.toString());

        if (result == null || !result.has("id") || !result.path("id").asText().equals(request.webhook_id)) {
            throw new ToolException("Fetch failed unexpected result");
        }

        return result;
    }

    static class Request {
        @JsonProperty(required = true)
        public String client_id;

        @JsonProperty(required = true)
        public String webhook_id;
    }
}
