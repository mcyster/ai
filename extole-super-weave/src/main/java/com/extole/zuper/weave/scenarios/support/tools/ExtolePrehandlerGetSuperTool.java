package com.extole.zuper.weave.scenarios.support.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.FatalToolException;
import com.cyster.ai.weave.service.tool.ToolException;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.client.web.ExtoleWebClientException;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.support.tools.ExtolePrehandlerGetSuperTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class ExtolePrehandlerGetSuperTool implements ExtoleSupportTool<Request> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleTrustedWebClientFactory.class);

    private ExtoleTrustedWebClientFactory extoleWebClientFactory;

    ExtolePrehandlerGetSuperTool(ExtoleTrustedWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("SuperTool", "");
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
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public Object execute(Request request, ExtoleSuperContext context, Weave weave) throws ToolException {
        JsonNode result;
        try {
            result = this.extoleWebClientFactory.getWebClientById(request.client_id).get()
                    .uri(uriBuilder -> uriBuilder.path("/v6/prehandlers/" + request.prehandler_id).build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
        } catch (ExtoleWebClientException | WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get clients", exception);
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
