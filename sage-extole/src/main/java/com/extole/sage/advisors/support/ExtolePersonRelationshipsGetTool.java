package com.extole.sage.advisors.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.assistant.impl.advisor.ToolException;
import com.cyster.assistant.service.advisor.FatalToolException;
import com.extole.sage.advisors.support.ExtolePersonRelationshipsGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class ExtolePersonRelationshipsGetTool implements ExtoleSupportAdvisorTool<Request> {
    private static final Logger logger = LogManager.getLogger(ExtoleWebClientFactory.class);

    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtolePersonRelationshipsGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extole_person_relationships_get";
    }

    @Override
    public String getDescription() {
        return "Get the relationships (to other people) associated with a person by person_id";
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
                    .path("/v4/runtime-persons/" + request.person_id + "/relationships")
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException.Forbidden exception) {
            // Should be a 404/400 not a 403
            var errorResponse = exception.getResponseBodyAs(JsonNode.class);
            if (errorResponse.has("code") && errorResponse.path("code").asText().equals("person_not_found")) {
                throw new ToolException("Person not found");
            }
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get person");
        }

        logger.trace("person.search result: " + result.toString());

        if (result == null || !result.isArray()) {
            throw new ToolException("Fetch failed unexpected result");
        }

        return result;
    }

    static class Request {
        @JsonPropertyDescription("The 1 to 12 digit id for a client.")
        @JsonProperty(required = true)
        public String client_id;

        @JsonPropertyDescription("The Extole id for the person")
        @JsonProperty(required = true)
        public String person_id;
    }
}
