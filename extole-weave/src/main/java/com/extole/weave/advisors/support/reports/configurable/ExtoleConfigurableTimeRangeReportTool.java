package com.extole.weave.advisors.support.reports.configurable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.extole.weave.advisors.support.ExtoleSupportTool;
import com.extole.weave.advisors.support.ExtoleWebClientFactory;
import com.extole.weave.advisors.support.reports.ExtoleReportBuilder;
import com.extole.weave.advisors.support.reports.configurable.UncachedExtoleConfigurableTimeRangeReportTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.annotation.JsonCreator;

class ExtoleConfigurableTimeRangeReportTool implements ExtoleSupportTool<Request> {
    Tool<Request, Void> tool;

    ExtoleConfigurableTimeRangeReportTool(Configuration configuration, ExtoleWebClientFactory extoleWebClientFactory) {

       this.tool = new UncachedExtoleConfigurableTimeRangeReportTool(
           configuration.getName(),
           configuration.getDescription(),
           configuration.getReportType(),
           configuration.getRowLimit(),
           configuration.getParameters(),
           configuration.waitForResult(),
           extoleWebClientFactory);
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
        private Map<String, String> parameters = new HashMap<>();
        private final int rowLimit;
        private final boolean waitForResult;

        @JsonCreator
        public Configuration(@JsonProperty("name") String name,
                @JsonProperty("description") String description,
                @JsonProperty("reportType") String reportType,
                @JsonProperty("parameters") Map<String, String> parameters,
                @JsonProperty("rowLimit") Integer rowLimit,
                @JsonProperty("waitForResult") Boolean waitForResult) {
            setName(name);
            setDescription(description);
            setReportType(reportType);
            setParameters(parameters);

            if (rowLimit != null) {
                this.rowLimit = rowLimit;
            } else {
                this.rowLimit = 10;
            }

            if (waitForResult != null) {
                this.waitForResult = waitForResult;
            } else {
                this.waitForResult = false;
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

        public boolean waitForResult() {
            return waitForResult;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        private void setParameters(Map<String, String> parameters) {
            this.parameters = parameters == null ? new HashMap<>() : parameters;
        }

        private void validateString(String value, String fieldName) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(fieldName + " cannot be null or empty");
            }
        }
    }
}


class UncachedExtoleConfigurableTimeRangeReportTool implements ExtoleSupportTool<Request> {
    private static final String PARAMETER_NAME_TIME_RANGE = "time_range";
    private static final String DEFAULT_TIME_RANGE = "LAST_MONTH";
    private ExtoleWebClientFactory extoleWebClientFactory;
    private String name;
    private String description;
    private String reportType;
    private int rowLimit;
    private Map<String, String> fixedParameters;
    private boolean waitForResult;

    public UncachedExtoleConfigurableTimeRangeReportTool(
            String name,
            String description,
            String reportType,
            int rowLimit,
            Map<String, String> fixedParameters,
            boolean waitForResult,
            ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
        this.name = name;
        this.description = description;
        this.reportType = reportType;
        this.rowLimit = rowLimit;
        this.fixedParameters = fixedParameters;
        this.waitForResult = waitForResult;
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
            ObjectMapper mapper = new ObjectMapper();
            fixedParameters.forEach((key, value) -> {
                JsonNode jsonNode = mapper.valueToTree(value);
                parameters.set(key, jsonNode);
            });

            if (request.timeRange != null && !request.timeRange.isBlank()) {
                parameters.put(PARAMETER_NAME_TIME_RANGE, request.timeRange);
            }
            if (!parameters.has(PARAMETER_NAME_TIME_RANGE)) {
                parameters.put(PARAMETER_NAME_TIME_RANGE, DEFAULT_TIME_RANGE);
            }
        }

        var reportBuilder = new ExtoleReportBuilder(this.extoleWebClientFactory)
                .withClientId(request.clientId)
                .withLimit(rowLimit)
                .withName(reportType)
                .withDisplayName(name)
                .withParameters(parameters)
                .withWaitForResult(waitForResult);

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

