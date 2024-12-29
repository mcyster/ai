package com.extole.admin.weave.scenarios.jsonpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.FatalToolException;
import com.cyster.ai.weave.service.tool.ToolException;
import com.extole.admin.weave.ExtoleAdminTool;
import com.extole.admin.weave.scenarios.jsonpath.ExtoleEventStreamEventsGetTool.Request;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.extole.client.web.ExtoleWebClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class ExtoleEventStreamEventsGetTool implements ExtoleAdminTool<Request> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleEventStreamEventsGetTool.class);

    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtoleEventStreamEventsGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Loads events from the specified event stream";
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

        if (request.eventStreamId == null || request.eventStreamId.isBlank()) {
            throw new ToolException("eventStreamId not specified");
        }

        final Integer offset = (request.offset != null) ? request.offset : 0;
        final Integer limit = (request.limit != null) ? request.limit : 2;

        JsonNode result;
        try {
            result = this.extoleWebClientFactory.getWebClient(context.getAccessToken()).get()
                    .uri(uriBuilder -> uriBuilder.path("/v6/event-streams/" + request.eventStreamId + "/events")
                            .queryParam("offset", String.valueOf(offset)).queryParam("limit", String.valueOf(limit))
                            .build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error", exception);
        }

        logger.trace("eventSteamEvent.fetch result: " + result.toString());

        return result;
    }

    static record Request(@JsonProperty(required = true) String eventStreamId,
            @JsonPropertyDescription("Offset into events to fetch, defaults to 0") @JsonProperty(required = false) Integer offset,
            @JsonPropertyDescription("Limit on the number of events to fetch, defaults to 2") @JsonProperty(required = false) Integer limit) {
    }

}
