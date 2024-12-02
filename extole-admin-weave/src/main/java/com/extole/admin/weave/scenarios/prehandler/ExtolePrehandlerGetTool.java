package com.extole.admin.weave.scenarios.prehandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.extole.admin.weave.ExtoleAdminTool;
import com.extole.admin.weave.scenarios.prehandler.ExtolePrehandlerGetTool.Request;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.client.web.ExtoleWebClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class ExtolePrehandlerGetTool implements ExtoleAdminTool<Request> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleTrustedWebClientFactory.class);

    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtolePrehandlerGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
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
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public Object execute(Request request, ExtoleSessionContext context, OperationLogger operation)
            throws ToolException {
        JsonNode result;
        try {
            result = this.extoleWebClientFactory.getWebClient(context.getAccessToken()).get()
                    .uri(uriBuilder -> uriBuilder.path("/v6/prehandlers/" + request.prehandlerId).build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get clients", exception);
        }

        logger.trace("prehandler.fetch result: " + result.toString());

        if (result == null || !result.has("id") || !result.path("id").asText().equals(request.prehandlerId)) {
            throw new ToolException("Fetch failed unexpected result");
        }

        return result;
    }

    static record Request(@JsonProperty(required = true) String prehandlerId) {
    }
}
