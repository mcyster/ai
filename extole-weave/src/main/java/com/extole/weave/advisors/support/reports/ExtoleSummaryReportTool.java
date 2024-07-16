package com.extole.weave.advisors.support.reports;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.ToolException;
import com.extole.weave.advisors.support.ExtoleSupportAdvisorTool;
import com.extole.weave.advisors.support.ExtoleWebClientFactory;
import com.extole.weave.advisors.support.reports.ExtoleSummaryReportTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class ExtoleSummaryReportTool implements ExtoleSupportAdvisorTool<Request> {
    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtoleSummaryReportTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extoleSummaryReport";
    }

    @Override
    public String getDescription() {
        return "Runs a report to count events";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {

        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("container", "production");

            var period = "WEEK";
            if (request.period != null && !request.period.isBlank()) {
                period = request.period;
            }
            parameters.put("period", period);

            var timeRange = "LAST_QUARTER";
            if (request.timeRange != null && !request.timeRange.isBlank()) {
                timeRange = request.timeRange;
            }
            parameters.put("time_range", timeRange);

            parameters.put("flows", "/business-events");

            parameters.put("include_totals", "false");

            var dimensions = "NONE";
            if (request.dimensions != null && !request.dimensions.isEmpty()) {
                for (var dimension : request.dimensions) {
                    switch (dimension) {
                    case "SOURCE":
                        // Work around bug, can't ask for SOURCE by itself
                        dimension = "SOURCE_TYPE,SOURCE";
                        break;
                    }
                    dimensions = String.join(",", request.dimensions);
                }
            }
            parameters.put("dimensions", dimensions);
        }

        var reportBuilder = new ExtoleReportBuilder(this.extoleWebClientFactory)
                .withClientId(request.clientId)
                .withLimit(12)
                .withName("summary")
                .withDisplayName("Summary Simple - AI")
                .withParameters(parameters);

        return reportBuilder.build();
    }

    static class Request {
        @JsonProperty(required = true)
        public String clientId;

        @JsonPropertyDescription("dimensions by which to segment the summary data, defaults to no dimensions. Supported dimensions are: PROGRAM, CAMPAIGN, SOURCE, CHANNEL, VARIANT, VISIT_TYPE and QUALITY")
        @JsonProperty(required = false)
        public List<String> dimensions;

        @JsonPropertyDescription("time range of report as an ISO date range, defaults to the last 12 weeks")
        @JsonProperty(required = false)
        public String timeRange;

        @JsonPropertyDescription("period for each row in the report, defaults to WEEK, Support periods include: HOUR, DAY, WEEK")
        @JsonProperty(required = false)
        public String period;

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
                   Objects.equals(dimensions, value.dimensions) &&
                   Objects.equals(timeRange, value.timeRange) &&
                   Objects.equals(period, value.period);

        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, dimensions, timeRange, period);
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

