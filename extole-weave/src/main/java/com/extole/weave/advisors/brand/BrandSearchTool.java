package com.extole.weave.advisors.brand;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.ai.weave.service.advisor.Tool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// https://docs.brandfetch.com/reference/get-started

class BrandSearchTool implements Tool<BrandSearchRequest, Void> {
    private Optional<String> brandFetchApiKey;

    BrandSearchTool(Optional<String> brandFetchApiKey) {
        this.brandFetchApiKey = brandFetchApiKey;
    }

    @Override
    public String getName() {
        return "brandSearch";
    }

    @Override
    public String getDescription() {
        return "Retrieve brand domains and logos that match the name or partial name provided";
    }

    @Override
    public Class<BrandSearchRequest> getParameterClass() {
        return BrandSearchRequest.class;
    }

    @Override
    public Object execute(BrandSearchRequest searchRequest, Void context) {
        var webClient = WebClient.builder().baseUrl("https://api.brandfetch.io/")
            .build();

        if (brandFetchApiKey.isEmpty()) {
            return toJsonNode("{ \"error\": \"brandFetchApiKey is required\" }");
        }

        var pathParameters = new HashMap<String, String>();
        pathParameters.put("query", searchRequest.query);

        var result = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/search/{query}")
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

class BrandSearchRequest {
    @JsonPropertyDescription("Brand name or part of brand name to find")
    @JsonProperty(required = true)
    public String query;
}

class BrandSearchResult {
    private String brandId;
    private String name;
    private String domain;
    private String icon;
    private Boolean claimed;

    public BrandSearchResult(String brandId, String name, String domain, String icon, Boolean claimed) {
        this.brandId = brandId;
        this.name = name;
        this.domain = domain;
        this.icon = icon;
        this.claimed = claimed;
    }

    public String getBrandId() {
        return brandId;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    public String getIcon() {
        return icon;
    }

    public Boolean isClaimed() {
        return claimed;
    }
}

class BrandSearchResponse {
    public List<BrandSearchResult> brands;

    public BrandSearchResponse(List<BrandSearchResult> brands) {
        this.brands = brands;
    }

    public List<BrandSearchResult> getContent() {
        return this.brands;
    }
}
