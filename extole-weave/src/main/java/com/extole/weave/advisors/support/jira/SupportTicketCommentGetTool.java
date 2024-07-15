package com.extole.weave.advisors.support.jira;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.FatalToolException;
import com.cyster.ai.weave.service.advisor.ToolException;
import com.cyster.jira.client.JiraWebClientFactory;
import com.extole.weave.advisors.support.ExtoleSupportAdvisorTool;
import com.extole.weave.advisors.support.jira.SupportTicketCommentGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class SupportTicketCommentGetTool implements ExtoleSupportAdvisorTool<Request> {
    private JiraWebClientFactory jiraWebClientFactory;

    SupportTicketCommentGetTool(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }

    @Override
    public String getName() {
        return "ticketCommentsGet";
    }

    @Override
    public String getDescription() {
        return "Retrieve comments on a ticket from the extole Jira support ticket tracking system";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        if (request.key == null || request.key.isEmpty()) {
            throw new FatalToolException("Attribute ticket key not specified");
        }

        final String startAt = request.rowOffset != null ? String.valueOf(request.rowOffset) : "0";
        final String maxResults = request.rowLimit != null ? String.valueOf(request.rowLimit) : "15";

        var result =  this.jiraWebClientFactory.getWebClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/rest/api/3/issue/" + request.key + "/comment")
                .queryParam("startAt", startAt)
                .queryParam("maxResults", maxResults)
                .queryParam("orderBy", "+created")
                .build())
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

        return result;
    }

    static class Request {
        @JsonPropertyDescription("ticket key")
        @JsonProperty(required = true)
        public String key;

        @JsonPropertyDescription("The index of the first comment in to return, default 0")
        @JsonProperty(required = false)
        public Integer rowOffset;

        @JsonPropertyDescription("The maxminmum number of comments to return in one request, default 15")
        @JsonProperty(required = false)
        public Integer rowLimit;
    }
}



