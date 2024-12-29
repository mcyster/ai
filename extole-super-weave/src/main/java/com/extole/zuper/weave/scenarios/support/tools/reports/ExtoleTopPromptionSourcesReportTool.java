package com.extole.zuper.weave.scenarios.support.tools.reports;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.ToolException;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.zuper.weave.scenarios.support.tools.reports.ExtoleTopPromptionSourcesReportTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class ExtoleTopPromptionSourcesReportTool implements ExtoleSupportTool<Request> {
    private ExtoleTrustedWebClientFactory extoleWebClientFactory;

    ExtoleTopPromptionSourcesReportTool(ExtoleTrustedWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Runs a report giving the traffic to the top promotion sources";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public Object execute(Request request, ExtoleSuperContext context, Weave weave) throws ToolException {

        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("container", "production");
            parameters.put("top_sources_count", "10");

            var period = "DAY";
            if (request.period != null && !request.period.isBlank()) {
                period = request.period;
            }
            parameters.put("period", period);

            var timeRange = "LAST_MONTH";
            if (request.timeRange != null && !request.timeRange.isBlank()) {
                timeRange = request.timeRange;
            }
            parameters.put("time_range", timeRange);

            parameters.put("visit_type", "ALL");
            parameters.put("steps", "promotion_viewed");
            parameters.put("sort_order", "descending(promotion_viewed)");
            parameters.put("quality_all", "ALL");

        }

        var reportBuilder = new ExtoleReportBuilder(this.extoleWebClientFactory).withClientId(request.clientId)
                .withLimit(12).withName("TOP_PROMOTION_SOURCES_V2").withDisplayName("Top Promotion Sources - AI")
                .withParameters(parameters).withWaitForResult(false);

        return reportBuilder.build();
    }

    static class Request {
        @JsonProperty(required = true)
        public String clientId;

        @JsonPropertyDescription("time range of report as an ISO date range, defaults to the last 4 weeks")
        @JsonProperty(required = false)
        public String timeRange;

        @JsonPropertyDescription("period for each row in the report, defaults to DAY, Support periods include: HOUR, DAY, WEEK")
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
            return Objects.equals(clientId, value.clientId) && Objects.equals(timeRange, value.timeRange)
                    && Objects.equals(period, value.period);

        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, timeRange, period);
        }

        @Override
        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException("Error converting object of class " + this.getClass().getName() + " JSON",
                        exception);
            }
        }
    }
}
