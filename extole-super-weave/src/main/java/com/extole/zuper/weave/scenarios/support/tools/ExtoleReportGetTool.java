package com.extole.zuper.weave.scenarios.support.tools;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.extole.client.web.ExtoleWebClientException;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleReportGetTool.Request;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class ExtoleReportGetTool implements ExtoleSupportTool<Request> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleTrustedWebClientFactory.class);

    private ExtoleTrustedWebClientFactory extoleWebClientFactory;

    ExtoleReportGetTool(ExtoleTrustedWebClientFactory extoleWebClientFactory) {
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
                    .path("/v4/reports/" + request.reportId())
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (ExtoleWebClientException exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get report", exception);
        }

        logger.trace("report.get result: " + result.toString());

        if (result == null || !result.has("report_id")) {
            throw new ToolException("Fetch failed unexpected result");
        }

        return result;
    }

    static record Request(
        @JsonPropertyDescription("The 1 to 12 digit id for a client.")
        @JsonProperty(required = true)
        String clientId,

        @JsonPropertyDescription("The id of the report")
        @JsonProperty(required = true)
        String reportId
    ) {}
}

