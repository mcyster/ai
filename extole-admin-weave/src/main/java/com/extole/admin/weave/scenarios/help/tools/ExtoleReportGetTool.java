package com.extole.admin.weave.scenarios.help.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.extole.admin.weave.scenarios.help.tools.ExtoleReportGetTool.Request;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.client.web.ExtoleWebClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ExtoleReportGetTool implements Tool<Request, ExtoleSessionContext> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleTrustedWebClientFactory.class);

    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtoleReportGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
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
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public Object execute(Request request, ExtoleSessionContext context, OperationLogger operation)
            throws ToolException {
        JsonNode result;

        try {
            result = this.extoleWebClientFactory.getWebClient(context.getAccessToken()).get()
                    .uri(uriBuilder -> uriBuilder.path("/v4/reports/" + request.reportId()).build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleUserToken is invalid", exception);
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
            @JsonPropertyDescription("The id of the report") @JsonProperty(required = true) String reportId) {
    }

}
