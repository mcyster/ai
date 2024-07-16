package com.cyster.weave.impl.scenarios.brand;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.ai.weave.service.Tool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// https://docs.brandfetch.com/reference/get-started

class BrandFetchTool implements Tool<BrandFetchRequest, Void> {
    private Optional<String> brandFetchApiKey;

    BrandFetchTool(Optional<String> brandFetchApiKey) {
        this.brandFetchApiKey = brandFetchApiKey;
    }

    @Override
    public String getName() {
        return "brandFetch";
    }

    @Override
    public String getDescription() {
        return "Retrives details about a brand given the brands domain name";
    }

    @Override
    public Class<BrandFetchRequest> getParameterClass() {
        return BrandFetchRequest.class;
    }

    @Override
    public Object execute(BrandFetchRequest fetchRequest, Void context) {
        var webClient = WebClient.builder().baseUrl("https://api.brandfetch.io/v2/brands/{domainName}")
            .build();

        if (brandFetchApiKey.isEmpty()) {
            return toJsonNode("{ \"error\": \"brandFetchApiKey is required\" }");
        }

        var pathParameters = new HashMap<String, String>();
        pathParameters.put("domainName", fetchRequest.domainName);

        var result = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .build(pathParameters))
            .header("Authorization", "Bearer " + brandFetchApiKey.get())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        return result;
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

class BrandFetchRequest {
    @JsonPropertyDescription("Domain name of brand to describe")
    @JsonProperty(required = true)
    public String domainName;
}
