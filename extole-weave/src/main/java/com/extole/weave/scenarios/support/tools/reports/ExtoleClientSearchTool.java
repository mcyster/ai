package com.extole.weave.scenarios.support.tools.reports;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.extole.client.web.ExtoleWebClientException;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.weave.scenarios.support.tools.reports.ExtoleClientSearchTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

@Component
class ExtoleClientSearchTool implements ExtoleSupportTool<Request> {
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
    public Object execute(Request searchRequest, Void context, OperationLogger operation) throws ToolException {

        JsonNode resultNode;
        try {
            resultNode = this.extoleWebClientFactory.getSuperUserWebClient().get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/clients")
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (ExtoleWebClientException | WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get clients");
        }

        if (resultNode == null || !resultNode.isArray()) {
            throw new ToolException("Query failed with unexpected result");
        }

        if (searchRequest.query == null || searchRequest.query.isEmpty()) {
            return resultNode;
        }
        var query = searchRequest.query.toLowerCase();

        ArrayNode results = JsonNodeFactory.instance.arrayNode();
        {
            for (JsonNode clientNode : resultNode) {
                if (clientNode.path("name").asText().toLowerCase().contains(query) ||
                    clientNode.path("short_name").asText().toLowerCase().contains(query) ||
                    clientNode.path("client_id").asText().equals(query)) {
                    results.add(clientNode);
                }
            }
        }

        return results;
    }

    static class Request {
        @JsonPropertyDescription("Query client list against client name, client short_name or client_id")
        @JsonProperty(required = false)
        public String query;
    }
}


