package com.extole.weave.scenarios.support.tools;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.client.web.ExtoleWebClientException;
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
public class ExtoleReportSchemaTool implements ExtoleSupportTool<Request> {
    private ExtoleTrustedWebClientFactory extoleWebClientFactory;

    ExtoleReportSchemaTool(ExtoleTrustedWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Get the json schema of a report given the client id and report id";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context, OperationLogger operation) throws ToolException {
        return getReportSchema(request.clientId(), request.reportId(), request.reportRunType());
    }

    private JsonSchema getReportSchema(String clientId, String reportId, ReportRunType runType) throws ToolException {
        JsonNode dataNodes = tryHardToloadReport(clientId, reportId, runType, 1);
        
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
   
    private JsonNode tryHardToloadReport(String clientId, String reportId, ReportRunType runType, int limit) throws ToolException {
        Optional<JsonNode> result;
        
        result = loadReport(clientId, reportId, runType, limit);
        if (result.isEmpty()) {
            if (runType == ReportRunType.REPORT) {
                runType = ReportRunType.REPORT_RUNNER;
            } else {
                runType = ReportRunType.REPORT;
            }
            result = loadReport(clientId, reportId, runType, limit);
            if (result.isEmpty()) {
                throw new ToolException("Unable to load report/report_runner");
            }
        }
        
        return result.get();
    }

    // Don't want to pass report to AI 
    private Optional<JsonNode> loadReport(String clientId, String reportId, ReportRunType runType, int limit) throws ToolException {
        
        String url;
        if (runType == ReportRunType.REPORT) {
            url = "/v4/reports/" + reportId + "/download.json";
        } else {
            url = "/v6/report-runners/" + reportId + "/latest/download.json";
        }
          
        JsonNode response;
        try {
            response = this.extoleWebClientFactory.getWebClientById(clientId).get()
                .uri(uriBuilder -> uriBuilder
                    .path(url)
                    .queryParam("limit", String.valueOf(limit))
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (ExtoleWebClientException | WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            return Optional.empty();
        } 

        return Optional.of(response);
    }
    
    public enum ReportRunType {
        @JsonProperty("Report")
        @JsonPropertyDescription("This report only runs once")
        REPORT,
        @JsonProperty("ReportRunner")
        @JsonPropertyDescription("This type of report runs multiple times. Scheduled reports are this type")
        REPORT_RUNNER
    }
    
    public static record Request(
        @JsonPropertyDescription("The 1 to 12 digit id for a client.")
        @JsonProperty(required = true)
        String clientId,

        @JsonPropertyDescription("The 20 to 22 character alphanumeric Extole report id")
        @JsonProperty(required = true)
        String reportId,
        
        @JsonPropertyDescription("Report type")
        @JsonProperty(required = false)
        ReportRunType reportRunType         
    ) {
        public Request(
            @JsonProperty("clientId") String clientId,
            @JsonProperty("reportId") String reportId,
            @JsonProperty("reportRunType") ReportRunType reportRunType
        ) {
            this.clientId = clientId;
            this.reportId = reportId;
            this.reportRunType = reportRunType != null ? reportRunType : ReportRunType.REPORT;
        }
    }
}

