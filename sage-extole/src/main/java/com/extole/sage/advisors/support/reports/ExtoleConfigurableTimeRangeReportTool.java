package com.extole.sage.advisors.support.reports;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.cyster.sherpa.impl.advisor.CachingTool;
import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.extole.sage.advisors.support.ExtoleSupportAdvisorTool;
import com.extole.sage.advisors.support.ExtoleWebClientFactory;
import com.extole.sage.advisors.support.reports.UncachedExtoleConfigurableTimeRangeReportTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonCreator;

class ExtoleConfigurableTimeRangeReportTool implements ExtoleSupportAdvisorTool<Request> {
    Tool<Request, Void> tool;
    
    ExtoleConfigurableTimeRangeReportTool(Configuration configuration, ExtoleWebClientFactory extoleWebClientFactory) {

       var tool = new UncachedExtoleConfigurableTimeRangeReportTool(
                configuration.getName(),
                configuration.getDescription(),
                configuration.getReportType(),
                configuration.getRowLimit(),
                configuration.getParameters(),
                extoleWebClientFactory);
        
        this.tool = CachingTool.builder(tool).build();
    }
    
    @Override
    public String getName() {
        return this.tool.getName();
    }

    @Override
    public String getDescription() {
        return this.tool.getDescription();
    }

    @Override
    public Class<Request> getParameterClass() {
        return this.tool.getParameterClass();
    }

    @Override
    public Object execute(Request parameters, Void context) throws ToolException {
        return this.tool.execute(parameters, context);
    }

    public static class Configuration {
        private String name;
        private String description;
        private String reportType;
        private final int rowLimit;
        private Map<String, String> parameters = new HashMap<>();

        @JsonCreator
        public Configuration(@JsonProperty("name") String name,
                @JsonProperty("description") String description,
                @JsonProperty("reportType") String reportType,
                @JsonProperty("parameters") Map<String, String> parameters,
                @JsonProperty("rowLimit") Integer rowLimit) {
            setName(name);
            setDescription(description);
            setReportType(reportType);
            setParameters(parameters);
            
            if (rowLimit != null) {
                this.rowLimit = rowLimit;
            } else {
                this.rowLimit = 10;
            }
        }
        
        public String getName() {
            return name;
        }

        private void setName(String name) {
            validateString(name, "name");
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        private void setDescription(String description) {
            validateString(description, "description");
            this.description = description;
        }

        public String getReportType() {
            return reportType;
        }

        private void setReportType(String reportType) {
            validateString(reportType, "reportType");
            this.reportType = reportType;
        }

        public int getRowLimit() {
            return rowLimit;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        private void setParameters(Map<String, String> parameters) {
            if (parameters == null || parameters.isEmpty()) {
                throw new IllegalArgumentException("Parameters cannot be null or empty");
            }
            this.parameters = parameters;
        }
        
        private void validateString(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(fieldName + " cannot be null or empty");
            }
        } 
    }
}


class UncachedExtoleConfigurableTimeRangeReportTool implements ExtoleSupportAdvisorTool<Request> {
    private ExtoleWebClientFactory extoleWebClientFactory;
    private String name;
    private String description;
    private String reportType;
    private int rowLimit;
    private Map<String, String> fixedParameters;
    
    public UncachedExtoleConfigurableTimeRangeReportTool(String name, String description, String reportType, int rowLimit, Map<String, String> fixedParameters, 
        ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;        
        this.name = name;
        this.name = name;
        this.reportType = reportType;
        this.rowLimit = rowLimit;
        this.fixedParameters = fixedParameters;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        { 
            var timeRange = "LAST_MONTH";
            if (request.timeRange != null && !request.timeRange.isBlank()) {
                timeRange = request.timeRange;
            }
            parameters.put("time_range", timeRange);
            
            ObjectMapper mapper = new ObjectMapper();
            fixedParameters.forEach((key, value) -> {
                JsonNode jsonNode = mapper.valueToTree(value);
                parameters.set(key, jsonNode);
            });

        }

        var reportBuilder = new ExtoleReportBuilder(this.extoleWebClientFactory)
                .withClientId(request.clientId)
                .withLimit(rowLimit) 
                .withName(reportType)
                .withDisplayName(name)
                .withParameters(parameters);
        
        return reportBuilder.build();
    }
    
    static class Request {
        @JsonProperty(required = true)
        public String clientId;

        @JsonPropertyDescription("time range of report as an ISO date range, defaults to the last 4 weeks")
        @JsonProperty(required = false)
        public String timeRange;

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) { 
                return false;
            }
            
            Request value = (Request) object;
            return Objects.equals(clientId, value.clientId) &&
                   Objects.equals(timeRange, value.timeRange);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, timeRange);
        }   
        
        @Override
        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException("Error converting object of class " + this.getClass().getName() + " JSON", exception);
            }
        }
    }
}

