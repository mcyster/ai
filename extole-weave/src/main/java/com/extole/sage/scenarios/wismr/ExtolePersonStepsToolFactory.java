package com.extole.sage.scenarios.wismr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.advisor.Tool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

class ExtolePersonStepsToolParameters {
    private String personId;
    private String stepName;

    public ExtolePersonStepsToolParameters(
        @JsonProperty("personId") String personId,
        @JsonProperty("stepName") String stepName) {
        this.personId = personId;
        this.stepName = stepName;

    }

    @JsonProperty("personId")
    public String getPersonId() {
        return this.personId;
    }

    @JsonProperty("stepName")
    public String getStepName() {
        return this.stepName;
    }
}

class ExtolePersonStepsTool implements Tool<ExtolePersonStepsToolParameters, Void> {
    private final WebClient.Builder webClientBuilder;
    private Optional<String> accessToken;

    public ExtolePersonStepsTool(WebClient.Builder builder, Optional<String> accessToken) {
        this.accessToken = accessToken;
        this.webClientBuilder = builder;
    }
    
    @Override
    public String getName() {
        return "person_steps";
    }

    @Override
    public String getDescription() {
        return "Get the steps a person has visited";
    }

    @Override
    public Class<ExtolePersonStepsToolParameters> getParameterClass() {
        return ExtolePersonStepsToolParameters.class;
    }

    @Override
    public Object execute(ExtolePersonStepsToolParameters parameters, Void context) {
        return this.getExecutor().apply((ExtolePersonStepsToolParameters)parameters);   
    }
    
    public Function<ExtolePersonStepsToolParameters, Object> getExecutor() {
        return parameter -> loadSteps(parameter);
    }

    private JsonNode loadSteps(ExtolePersonStepsToolParameters parameters) {
        var webClient = this.webClientBuilder.baseUrl("https://api.extole.io/v4/runtime-persons/{personId}/steps")
            .build();

        if (accessToken.isEmpty()) {
            return toJsonNode("{ \"error\": \"access_token_required\" }");
        }

        if (parameters.getPersonId() == null || parameters.getPersonId().isBlank()) {
            return toJsonNode("{ \"error\": \"person_id_not_specifid\" }");
        }

        var queryParameters = new LinkedMultiValueMap<String, String>();
        queryParameters.add("stepName", "converted");

        var pathParameters = new HashMap<String, String>();
        pathParameters.put("personId", parameters.getPersonId());

        JsonNode jsonNode;
        try {
            jsonNode = webClient.get().uri(uriBuilder -> uriBuilder
                .queryParams(queryParameters)
                .build(pathParameters))
                .header("Authorization", "Bearer " + this.accessToken.get())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException exception) {
            if (exception.getStatusCode().value() == 403) {
                return toJsonNode("{ \"error\": \"access_denied\" }");
            } else if (exception.getStatusCode().is4xxClientError()) {
                return toJsonNode("{ \"error\": \"bad_request\" }");
            } else {
                return toJsonNode("{ \"error\": \"something_went_wrong\" }");
            }
        }

        var steps = new ArrayList<Map<String, String>>();
        if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (var stepNode : arrayNode) {
                String name = stepNode.get("step_name").toString();
                String id = stepNode.get("id").toString();
                var step = new HashMap<String, String>();
                step.put("step_name", name);
                step.put("id", id);
                steps.add(step);
            }
        } else {
            return toJsonNode("{ \"error\": \"steps_not_an_array\" }");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(steps);
    }

    private static JsonNode toJsonNode(String json) {
        JsonNode jsonNode;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonNode = objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Unable to parse Json response", exception);
        }
        return jsonNode;
    }
}

@Component
public class ExtolePersonStepsToolFactory {
    private final WebClient.Builder webClientBuilder;

    public ExtolePersonStepsToolFactory(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    ExtolePersonStepsTool create(Optional<String> accessToken) {
        return new ExtolePersonStepsTool(this.webClientBuilder, accessToken);
    }
}
