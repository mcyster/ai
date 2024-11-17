package com.cyster.weave.impl.scenarios.brand;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.Tool;
import com.cyster.weave.impl.scenarios.brand.BrandSearchTool.BrandSearchRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// https://docs.brandfetch.com/reference/get-started

@Component
@Conditional(BrandEnabledCondition.class)
class BrandSearchTool implements Tool<BrandSearchRequest, Void> {
    private String brandFetchApiKey;

    BrandSearchTool(@Value("${brandFetchApiKey:#{environment.BRANDFETCH_API_KEY}}") String brandFetchApiKey) {
        this.brandFetchApiKey = brandFetchApiKey;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
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
    public Object execute(BrandSearchRequest searchRequest, Void context, OperationLogger operation) {
        var webClient = WebClient.builder().baseUrl("https://api.brandfetch.io/").build();

        if (brandFetchApiKey.isEmpty()) {
            return toJsonNode("{ \"error\": \"brandFetchApiKey is required\" }");
        }

        var pathParameters = new HashMap<String, String>();
        pathParameters.put("query", searchRequest.query);

        var result = webClient.get().uri(uriBuilder -> uriBuilder.path("/v2/search/{query}").build(pathParameters))
                .header("Authorization", "Bearer " + brandFetchApiKey).accept(MediaType.APPLICATION_JSON).retrieve()
                .bodyToMono(JsonNode.class).block();

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

    static record BrandSearchRequest(
            @JsonPropertyDescription("Brand name or part of brand name to find") @JsonProperty(required = true) String query) {
    }
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
