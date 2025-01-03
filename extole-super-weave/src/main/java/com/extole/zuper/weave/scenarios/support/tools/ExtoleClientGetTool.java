package com.extole.zuper.weave.scenarios.support.tools;

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
import com.extole.zuper.weave.scenarios.support.tools.ExtoleClientGetTool.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ExtoleClientGetTool implements ExtoleSupportTool<Parameters> {
    private ExtoleTrustedWebClientFactory extoleWebClientFactory;

    ExtoleClientGetTool(ExtoleTrustedWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Get a client by clientId";
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public Object execute(Parameters parameters, ExtoleSuperContext context, Weave weave) throws ToolException {
        JsonNode result;

        try {
            result = this.extoleWebClientFactory.getSuperUserWebClient().get()
                    .uri(uriBuilder -> uriBuilder.path("/v4/clients/" + parameters.clientId()).build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
        } catch (ExtoleWebClientException | WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get client");
        }

        if (result == null || !result.has("client_id")) {
            throw new ToolException("Fetch failed unexpected result");
        }

        return result;
    }

    static record Parameters(
            @JsonPropertyDescription("The 1 to 12 digit id for a client.") @JsonProperty(required = true) String clientId) {
    }
}
