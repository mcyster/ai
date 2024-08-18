package com.extole.weave.scenarios.support.tools;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.extole.client.web.ExtoleWebClientException;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.weave.scenarios.support.tools.ExtoleReportDownloadTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Output;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Version;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import reactor.core.publisher.Mono;

@Component
class ExtoleReportDownloadTool implements ExtoleSupportTool<Request> {
    private static final Logger logger = LogManager.getLogger(ExtoleTrustedWebClientFactory.class);

    private ExtoleTrustedWebClientFactory extoleWebClientFactory;

    ExtoleReportDownloadTool(ExtoleTrustedWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Gets details of the report like status (DONE, IN_PROGRESS), name and parameters used to run report.";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context, OperationLogger operation) throws ToolException {
        JsonNode result;

        try {
            result = this.extoleWebClientFactory.getWebClientById(request.clientId()).get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/reports/" + request.reportId() + "/download.json")
                    .queryParam("offset", request.offset())
                    .queryParam("limit", request.limit())
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(WebClientResponseException.class, exception -> {
                    if (exception.getCause() instanceof DataBufferLimitException) {
                        return Mono.error(new ToolException("Data buffer limit exceeded, perhaps reduce the row limit", exception));
                    }
                    return Mono.error(exception);
                })                
                .block();
        } catch (ExtoleWebClientException exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get report", exception);
        }

        logger.trace("report.download result: " + result.toString());

        if (result == null || !result.isArray()) {
            throw new ToolException("Fetch failed unexpected result");
        }
        
        if (request.jqExpression != null && !request.jqExpression.isBlank()) {
            ArrayNode jqResults = JsonNodeFactory.instance.arrayNode();

            try {
                Scope rootScope = Scope.newEmptyScope();
                Scope childScope = Scope.newChildScope(rootScope);
    
                JsonQuery jq = JsonQuery.compile(".[] | {event_id, name, message}", Version.LATEST);
                
                Output output = (JsonNode item) -> {
                    jqResults.add(item);
                };
                
                jq.apply(childScope, result, output);
                
            } catch (JsonQueryException exception) {
                throw new ToolException("Failed to run report through jq expression: " + request.jqExpression, exception);
            }
            result = jqResults;
        }
        
        return result;
    }

    static record Request(
        @JsonPropertyDescription("The 1 to 12 digit id for a client.")
        @JsonProperty(required = true)
        String clientId,

        @JsonPropertyDescription("The id of the report")
        @JsonProperty(required = true)
        String reportId,
        
        @JsonPropertyDescription("The row of the report to start downloading from, defaults to 0")
        @JsonProperty(required = false)
        Integer offset,
        
        @JsonPropertyDescription("The maximum number of rows to download from the report, defaults to 10")
        @JsonProperty(required = false)
        Integer limit,

        @JsonPropertyDescription("Parse the output of the report through the jq expression before returning")
        @JsonProperty(required = false)
        String jqExpression
    ) {
        public Request(
                String clientId,
                String reportId,
                Integer offset,
                Integer limit,
                String jqExpression) {
            this.clientId = clientId;
            this.reportId = reportId;
            this.offset = offset == null ? 0 : offset;
            this.limit = limit == null ? 10 : limit;
            this.jqExpression = jqExpression;
        }
    }
}

