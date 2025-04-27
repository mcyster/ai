package com.extole.admin.weave.scenarios.help.tools;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.FatalToolException;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.ToolException;
import com.extole.admin.weave.scenarios.help.tools.ExtoleConfigurableReportPostTool.Request;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.extole.client.common.TimePeriod;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.client.web.ExtoleWebClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class ExtoleConfigurableReportPostTool implements Tool<Request, ExtoleSessionContext> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleTrustedWebClientFactory.class);
    private static final String PARAMETER_NAME_TIME_RANGE = "time_range";
    private static final String DEFAULT_TIME_RANGE = "LAST_MONTH";

    enum BaseEventType {
        STEP("CONFIGURABLE_EVENT_METRICS"), REWARD("CONFIGURABLE_REWARD_EVENT_METRICS"),
        MESSAGE("CONFIGURABLE_MESSAGE_SUMMARY_METRICS"), CLIENT("CONFIGURABLE_CLIENT_EVENT_METRICS"),
        INPUT("CONFIGURABLE_INPUT_RECORD_METRICS");

        private String reportType;

        BaseEventType(String reportType) {
            this.reportType = reportType;
        }

        public String getReportType() {
            return reportType;
        }

        private static final Map<String, BaseEventType> BY_NAME = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(Enum::name, e -> e));

        public static BaseEventType fromNameOrDefault(String name) {
            if (name == null) {
                return STEP;
            }
            return BY_NAME.getOrDefault(name.toUpperCase(), STEP);
        }

        public static String getReportTypeOrDefault(String name) {
            return fromNameOrDefault(name).getReportType();
        }
    }

    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtoleConfigurableReportPostTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Runs the configurable report, returning the report_id";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public Object execute(Request request, ExtoleSessionContext context, Weave weave) throws ToolException {
        JsonNode result;

        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            if (request.timeRange != null && !request.timeRange.isBlank()) {
                var timeRange = new TimePeriod(request.timeRange).convertToTimeRange();
                parameters.put(PARAMETER_NAME_TIME_RANGE, timeRange);
            }
            if (!parameters.has(PARAMETER_NAME_TIME_RANGE)) {
                parameters.put(PARAMETER_NAME_TIME_RANGE, DEFAULT_TIME_RANGE);
            }

            parameters.put("mappings", request.mappings);
        }

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            payload.put("name", toSafeName(request.displayName));
            payload.put("display_name", request.displayName);
            payload.put("report_type", BaseEventType.fromNameOrDefault(request.baseEventType).getReportType());

            ArrayNode formatsArray = JsonNodeFactory.instance.arrayNode();
            formatsArray.add("JSONL");
            payload.set("formats", formatsArray);

            payload.set("parameters", parameters);
        }

        logger.info("payload: " + payload.toPrettyString());

        try {
            result = this.extoleWebClientFactory.getWebClient(context.getAccessToken()).post()
                    .uri(uriBuilder -> uriBuilder.path("/v4/reports").build()).accept(MediaType.APPLICATION_JSON)
                    .bodyValue(payload).retrieve().bodyToMono(JsonNode.class).block();
        } catch (WebClientResponseException.BadRequest exception) {
            String errorBody = exception.getResponseBodyAsString();
            throw new ToolException("Bad Request: " + errorBody, exception);
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get report", exception);
        }

        if (result == null || !result.has("report_id")) {
            throw new ToolException("Fetch failed unexpected result");
        }

        return result;
    }

    private static String toSafeName(String input) {
        if (input == null) {
            return "";
        }
        String safe = input.replaceAll("[^a-zA-Z0-9_]", "_");
        if (safe.length() > 60) {
            safe = safe.substring(0, 60);
        }
        return safe.toLowerCase();
    }

    static record Request(
            @JsonPropertyDescription("Short descriptive name for report") @JsonProperty(required = true) String displayName,
            @JsonPropertyDescription("Base event type of report, defaults to step. Event types: step, reward, message, client, inputRecord, input") @JsonProperty(required = false) String baseEventType,
            @JsonPropertyDescription("Time range of report as an ISO date range, defaults to "
                    + DEFAULT_TIME_RANGE) @JsonProperty(required = false) String timeRange,
            @JsonPropertyDescription("Column mappings of the form column_name1=expression1;column2=expression2;...") @JsonProperty(required = true) String mappings) {
    }

}
