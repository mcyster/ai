package com.extole.weave.scenarios.support.tools;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.extole.weave.scenarios.support.tools.ExtoleReportSchemaTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;
import com.fasterxml.jackson.module.jsonSchema.jakarta.types.ObjectSchema;


@Component
class ExtoleReportSchemaTool implements ExtoleSupportTool<Request> {
    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtoleReportSchemaTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Get a person by person_id";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        return getReportSchema(request.clientId(), request.reportId());
    }

    private JsonSchema getReportSchema(String clientId, String reportId) throws ToolException {
        JsonNode dataNodes = loadReport(clientId, reportId, 1);
        
        try {
            System.out.println("dataNodes: " + (new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValueAsString(dataNodes));
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (!dataNodes.isArray()) {
            throw new FatalToolException("Report " + reportId + " did not return an array of rows");            
        }
        if (dataNodes.size() == 0) {
            throw new FatalToolException("Report " + reportId + " is empty, can't determine schema");
        }
        JsonNode dataNode = dataNodes.get(0);

        ObjectMapper objectMapper = new ObjectMapper();

        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(objectMapper);

        Map<?, ?> dataMap = objectMapper.convertValue(dataNode, Map.class);

        JsonSchema dataSchema;
        try {
            dataSchema = schemaGen.generateSchema(dataMap.getClass());
            
            ObjectSchema objectSchema = (ObjectSchema) dataSchema;
            for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
                JsonSchema propertySchema = schemaGen.generateSchema(entry.getValue().getClass());
                objectSchema.putProperty(entry.getKey().toString(), propertySchema);
            }
        } catch (JsonMappingException exception) {
            throw new FatalToolException("Report " + reportId + " could not determine schema", exception);
        }
        
        return dataSchema;
    }
    
    // Don't want to pass report to AI 
    private JsonNode loadReport(String clientId, String reportId, int limit) throws ToolException {
        JsonNode response;
        try {
            response = this.extoleWebClientFactory.getWebClient(clientId).get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/reports/" + reportId + "/download.json")
                    .queryParam("limit", String.valueOf(limit))
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new FatalToolException("Problems fetching report: " + reportId, exception);
        } 

        return response;
    }
    static record Request(
        @JsonPropertyDescription("The 1 to 12 digit id for a client.")
        @JsonProperty(required = true)
        String clientId,

        @JsonPropertyDescription("The 20 to 22 character alphanumeric Extole report id")
        @JsonProperty(required = true)
        String reportId
    ) {};
}

