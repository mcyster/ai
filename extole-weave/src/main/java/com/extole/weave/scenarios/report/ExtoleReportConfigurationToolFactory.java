package com.extole.weave.scenarios.report;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.advisor.Tool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ExtoleReportConfigurationToolParameters {
    public String id;

    public ExtoleReportConfigurationToolParameters(@JsonProperty("id") String id) {
        this.id = id;
    }

    @JsonProperty("id")
    public String getId() {
        return this.id;
    }
}

class ExtoleReportConfigurationTool implements Tool<ExtoleReportConfigurationToolParameters, Void> {
    private final WebClient.Builder webClientBuilder;
    private Optional<String> accessToken;

    public ExtoleReportConfigurationTool(WebClient.Builder builder, Optional<String> accessToken) {
        this.accessToken = accessToken;
        this.webClientBuilder = builder;
    }

    @Override
    public String getName() {
        return "get_report_configuration";
    }

    @Override
    public String getDescription() {
        return "Get the configuration of a report given the report_id";
    }

    @Override
    public Class<ExtoleReportConfigurationToolParameters> getParameterClass() {
        return ExtoleReportConfigurationToolParameters.class;
    }

    @Override
    public Object execute(ExtoleReportConfigurationToolParameters parameters, Void context) {
        return this.getExecutor().apply((ExtoleReportConfigurationToolParameters)parameters);   
    }
    
    public Function<ExtoleReportConfigurationToolParameters, Object> getExecutor() {
        return reportHandle -> reportConfigurationLoader(reportHandle);
    }

    private JsonNode reportConfigurationLoader(ExtoleReportConfigurationToolParameters reportHandle) {
        var webClient = this.webClientBuilder.baseUrl("https://api.extole.io/v4/reports").build();

        if (accessToken.isEmpty()) {
            return toJsonNode("{ \"error\": \"access_token_required\" }");
        }

        JsonNode jsonNode;
        try {
            jsonNode = webClient.get().uri("/{id}", reportHandle.id)
                .header("Authorization", "Bearer " + this.accessToken.get())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ObjectNode.class)
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
public class ExtoleReportConfigurationToolFactory {
    private final WebClient.Builder webClientBuilder;

    public ExtoleReportConfigurationToolFactory(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public ExtoleReportConfigurationTool create(Optional<String> accessToken) {
        return new ExtoleReportConfigurationTool(this.webClientBuilder, accessToken);
    }
}
