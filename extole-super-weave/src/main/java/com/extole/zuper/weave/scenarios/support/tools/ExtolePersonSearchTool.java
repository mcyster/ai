package com.extole.zuper.weave.scenarios.support.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.FatalToolException;
import com.cyster.ai.weave.service.tool.ToolException;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.client.web.ExtoleWebClientException;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.support.tools.ExtolePersonSearchTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
class ExtolePersonSearchTool implements ExtoleSupportTool<Request> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleTrustedWebClientFactory.class);

    private ExtoleTrustedWebClientFactory extoleWebClientFactory;

    ExtolePersonSearchTool(ExtoleTrustedWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Find a person by a key";
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
        if (request.person_id != null) {
            return executeGet(request.client_id, request.person_id);
        } else {
            return executeSearch(request);
        }
    }

    private Object executeGet(String clientId, String personId) throws ToolException {
        JsonNode result;

        try {
            result = this.extoleWebClientFactory.getWebClientById(clientId).get()
                    .uri(uriBuilder -> uriBuilder.path("/v4/runtime-persons/" + personId).build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
        } catch (ExtoleWebClientException exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientResponseException.Forbidden exception) {
            // Should be a 404/400 not a 403
            var errorResponse = exception.getResponseBodyAs(JsonNode.class);
            if (errorResponse.has("code") && errorResponse.path("code").asText().equals("person_not_found")) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.createArrayNode();
            }
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get person(s)");
        }

        logger.trace("person.search result: " + result.toString());

        if (result == null || !result.has("id")) {
            throw new ToolException("Fetch failed unexpected result");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        var arrayNode = objectMapper.createArrayNode();
        arrayNode.add(result);

        return arrayNode;
    }

    private Object executeSearch(Request request) throws ToolException {
        JsonNode result;

        MultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>();
        {
            if (request.email != null) {
                queryParameters.add("email", request.email);
            }
            if (request.partner_user_id != null) {
                queryParameters.add("partner_user_id", request.partner_user_id);
            }
            if (request.partner_id != null) {
                queryParameters.add("partner_id", request.partner_id.name + ":" + request.partner_id.id);
            }
        }
        if (queryParameters.isEmpty()) {
            throw new ToolException("At least one person key must be specified");
        }

        try {
            result = this.extoleWebClientFactory.getWebClientById(request.client_id).get()
                    .uri(uriBuilder -> uriBuilder.path("/v4/runtime-persons/").queryParams(queryParameters).build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
        } catch (ExtoleWebClientException | WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get person(s)");
        }

        logger.trace("person.search result: " + result.toString());

        if (result == null || !result.isArray()) {
            throw new ToolException("Fetch failed unexpected result");
        }

        return result;
    }

    static class PartnerId {
        @JsonPropertyDescription("Name of property to identify the user, e.g. order_id, application_id")
        @JsonProperty(required = true)
        String name;

        @JsonProperty(required = true)
        String id;
    }

    static class Request {
        @JsonPropertyDescription("The 1 to 12 digit id for a client.")
        @JsonProperty(required = true)
        public String client_id;

        @JsonPropertyDescription("The email address of the person. Make sure to URL encode.")
        @JsonProperty(required = false)
        public String email;

        @JsonPropertyDescription("The Extole id for the person")
        @JsonProperty(required = false)
        public String person_id;

        @JsonPropertyDescription("The id of the person as used by the client")
        @JsonProperty(required = false)
        public String partner_user_id;

        @JsonPropertyDescription("An id unique to the user, such as order_id, application_id etc")
        @JsonProperty(required = false)
        public PartnerId partner_id;
    }
}
