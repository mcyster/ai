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
import com.extole.zuper.weave.scenarios.support.tools.ExtoleClientSearchTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

@Component
public class ExtoleClientSearchTool implements ExtoleSupportTool<Request> {
    private ExtoleTrustedWebClientFactory extoleWebClientFactory;

    ExtoleClientSearchTool(ExtoleTrustedWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Finds an Extole client by client name, clientShortName or clientId";
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

        JsonNode resultNode;
        try {
            resultNode = this.extoleWebClientFactory.getSuperUserWebClient().get()
                    .uri(uriBuilder -> uriBuilder.path("/v4/clients").queryParam("type", request.clientType()).build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
        } catch (ExtoleWebClientException | WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get clients");
        }

        if (resultNode == null || !resultNode.isArray()) {
            throw new ToolException("Query failed with unexpected result");
        }

        ArrayNode results = JsonNodeFactory.instance.arrayNode();
        {
            for (JsonNode clientNode : resultNode) {
                results.add(clientNode);
            }
        }

        return results;
    }

    static record Request(
            @JsonPropertyDescription("Filters client by type (CUSTOMER, EX_CUSTOMER, PROSPECT, UNCLASSIFIED, TEST), defaults to CUSTOMER") @JsonProperty(required = false) String clientType) {
        public Request(String clientType) {
            this.clientType = clientType == null ? "CUSTOMER" : clientType;
        }
    }
}
