package com.extole.weave.scenarios.support.tools.jira;

import java.util.Iterator;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.extole.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.weave.scenarios.support.tools.jira.SupportTicketSearchTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class SupportTicketSearchTool implements ExtoleSupportTool<Request> {
    private JiraWebClientFactory jiraWebClientFactory;

    SupportTicketSearchTool(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Retrieve tickets from the extole Jira ticket tracking system";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request searchRequest, Void context, OperationLogger operation) throws ToolException {

        String jql = "project = SUP";
        if (searchRequest.query != null && !searchRequest.query.isEmpty()) {
            jql = searchRequest.query;
        }

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
          payload.putArray("expand");

          ArrayNode fields = payload.putArray("fields");
          fields.add("summary");
          fields.add("status");
          fields.add("customfield_11312");
          fields.add("created");
          fields.add("updated");
          fields.add("priority");
          fields.add("parent");
          fields.add("assignee");
          fields.add("labels");
          payload.put("fieldsByKeys", false);
          payload.put("jql", jql);
          payload.put("maxResults", 15);
          payload.put("startAt", 0);
        }

        JsonNode resultNode;
        try {
            resultNode = this.jiraWebClientFactory.getWebClient().post()
                .uri(uriBuilder -> uriBuilder.path("/rest/api/3/search").build())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException exception) {
            throw new ToolException("Search failed with error. Payload: " + payload, exception);
        }

        if (resultNode == null || resultNode.path("issues").isEmpty()) {
            throw new ToolException("Search failed with unexpected internal error");
        }
        JsonNode issuesNode = resultNode.path("issues");

        // TODO more robust pattern to refine result
        ObjectNode results = JsonNodeFactory.instance.objectNode();
        {
            ArrayNode tickets = results.putArray("tickets");
            int rowCount = 0;
            for (JsonNode issueNode : issuesNode) {
                ObjectNode ticket = tickets.addObject();
                ticket.put("key", issueNode.path("key").asText());
                ticket.put("summary", issueNode.path("fields").path("summary").asText());

                JsonNode assignee = issueNode.path("fields").path("assignee");
                if (!assignee.isMissingNode()) {
                    ticket.put("assignee", assignee.path("emailAddress").asText());
                } else {
                    ticket.putNull("assignee");
                }

                JsonNode status = issueNode.path("fields").path("status");
                if (!status.isMissingNode()) {
                    ticket.put("status", status.path("name").asText());
                } else {
                    ticket.putNull("status");
                }

                JsonNode parent = issueNode.path("fields").path("parent");
                if (!parent.isMissingNode()) {
                    ticket.put("classification", parent.path("fields").path("summary").asText());
                } else {
                    ticket.putNull("classification");
                }

                JsonNode customField11312 = issueNode.path("fields").path("customfield_11312");
                if (!customField11312.isMissingNode()) {
                    ticket.put("client", customField11312.path("value").asText());
                } else {
                    ticket.putNull("client");
                }

                ticket.put("createdDate", issueNode.path("fields").path("created").asText());
                ticket.put("updatedDate", issueNode.path("fields").path("updated").asText());

                Iterator<JsonNode> labelsNode = issueNode.path("fields").path("labels").elements();
                ArrayNode labels = ticket.putArray("labels");
                while (labelsNode.hasNext()) {
                    JsonNode label = labelsNode.next();
                    labels.add(label.asText());
                }
                rowCount++;
            }

            results.put("totalRowCount", resultNode.path("total").asInt());

            ObjectNode page = results.putObject("page");

            page.put("rowStart", resultNode.path("startAt").asInt());
            page.put("rowCount", rowCount);
            page.put("rowLimit", resultNode.path("maxResults").asInt());
        }

        return results;
    }

    static class Request {
        @JsonPropertyDescription("JQL query for tickets, always prefix query with: project = SUP, if you have a clientId do a contains operation with the field name \"Client Id Calculated\"")
        @JsonProperty(required = false)
        public String query;

        @JsonPropertyDescription("The index of the first row in the result set to return, default 0")
        @JsonProperty(required = false)
        public int rowOffset;

        @JsonPropertyDescription("The maxminmum number of rows to return in one request, default 15")
        @JsonProperty(required = false)
        public int rowLimit;
    }
}


