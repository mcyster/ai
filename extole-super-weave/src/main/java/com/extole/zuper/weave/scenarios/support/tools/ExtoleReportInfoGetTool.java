package com.extole.zuper.weave.scenarios.support.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.FatalToolException;
import com.cyster.ai.weave.service.tool.ToolException;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.client.web.ExtoleWebClientException;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleReportInfoGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class ExtoleReportInfoGetTool implements ExtoleSupportTool<Request> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleTrustedWebClientFactory.class);

    private ExtoleTrustedWebClientFactory extoleWebClientFactory;

    ExtoleReportInfoGetTool(ExtoleTrustedWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "When the report is DONE, gives the number of rows in the report";
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
        JsonNode result;

        try {
            result = this.extoleWebClientFactory.getWebClientById(request.clientId()).get()
                    .uri(uriBuilder -> uriBuilder.path("/v4/reports/" + request.reportId() + "/info").build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
        } catch (ExtoleWebClientException exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get report", exception);
        }

        logger.trace("report.info.get result: " + result.toString());

        if (result == null || !result.has("total_rows")) {
            throw new ToolException("Fetch failed unexpected result");
        }

        return result;
    }

    static record Request(
            @JsonPropertyDescription("The 1 to 12 digit id for a client.") @JsonProperty(required = true) String clientId,

            @JsonPropertyDescription("The id of the report") @JsonProperty(required = true) String reportId) {
    }
}
