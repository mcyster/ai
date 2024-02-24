package com.extole.sage.advisors.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.sherpa.impl.advisor.FatalToolException;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.extole.sage.advisors.support.ExtolePrehandlerGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class ExtolePrehandlerGetTool implements ExtoleSupportAdvisorTool<Request> {
    private static final Logger logger = LogManager.getLogger(ExtoleWebClientFactory.class);

    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtolePrehandlerGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extole_prehandler_get";
    }

    @Override
    public String getDescription() {
        return "Loads the prehandler configuration by prehandler_id";
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
                    .path("/v6/prehandlers/" + request.prehandler_id)
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

        logger.trace("prehandler.fetch result: " + result.toString());

        if (result == null || !result.has("id") || !result.path("id").asText().equals(request.prehandler_id)) {
            throw new ToolException("Fetch failed unexpected result");
        }

        return result;
    }

    static class Request {
        @JsonProperty(required = true)
        public String client_id;

        @JsonProperty(required = true)
        public String prehandler_id;
    }
}