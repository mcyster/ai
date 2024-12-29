package com.extole.admin.weave.scenarios.wismr;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.Tool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ExtolePersonRewardsToolParameters {
    private String personId;

    public ExtolePersonRewardsToolParameters(@JsonProperty("personId") String personId) {
        this.personId = personId;
    }

    @JsonProperty("personId")
    public String getPersonId() {
        return this.personId;
    }
}

class ExtolePersonRewardsTool implements Tool<ExtolePersonRewardsToolParameters, Void> {
    private final WebClient.Builder webClientBuilder;
    private Optional<String> accessToken;

    public ExtolePersonRewardsTool(WebClient.Builder builder, Optional<String> accessToken) {
        this.accessToken = accessToken;
        this.webClientBuilder = builder;
    }

    @Override
    public String getName() {
        return "person_rewards";
    }

    @Override
    public String getDescription() {
        return "Get a persons reward given their profile_id";
    }

    @Override
    public Class<ExtolePersonRewardsToolParameters> getParameterClass() {
        return ExtolePersonRewardsToolParameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Object execute(ExtolePersonRewardsToolParameters parameters, Void context, Weave weave) {
        return this.getExecutor().apply((ExtolePersonRewardsToolParameters) parameters);
    }

    public Function<ExtolePersonRewardsToolParameters, Object> getExecutor() {
        return parameter -> loadRewards(parameter);
    }

    private JsonNode loadRewards(ExtolePersonRewardsToolParameters parameters) {
        var webClient = this.webClientBuilder.baseUrl("https://api.extole.io/v4/runtime-persons/{personId}/rewards")
                .build();

        if (accessToken.isEmpty()) {
            return toJsonNode("{ \"error\": \"access_token_required\" }");
        }

        if (parameters.getPersonId() == null || parameters.getPersonId().isBlank()) {
            return toJsonNode("{ \"error\": \"person_id_not_specifid\" }");
        }

        var queryParameters = new LinkedMultiValueMap<String, String>();

        var pathParameters = new HashMap<String, String>();
        pathParameters.put("personId", parameters.getPersonId());

        JsonNode jsonNode;
        try {
            jsonNode = webClient.get().uri(uriBuilder -> uriBuilder.queryParams(queryParameters).build(pathParameters))
                    .header("Authorization", "Bearer " + this.accessToken.get()).accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(JsonNode.class).block();
        } catch (WebClientResponseException exception) {
            if (exception.getStatusCode().value() == 403) {
                return toJsonNode("{ \"error\": \"access_denied\" }");
            } else if (exception.getStatusCode().is4xxClientError()) {
                return toJsonNode("{ \"error\": \"bad_request\" }");
            } else {
                return toJsonNode("{ \"error\": \"something_went_wrong\" }");
            }
        }

        return jsonNode;
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
public class ExtolePersonRewardsToolFactory {
    private final WebClient.Builder webClientBuilder;

    public ExtolePersonRewardsToolFactory(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    ExtolePersonRewardsTool create(Optional<String> accessToken) {
        return new ExtolePersonRewardsTool(this.webClientBuilder, accessToken);
    }
}
