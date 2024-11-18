package com.cyster.jira.client.ticket;

import org.springframework.http.MediaType;

import com.cyster.jira.client.adf.writer.AtlassianDocumentMapper;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

public class TicketCommentBuilder {
    private JiraWebClientFactory jiraWebClientFactory;
    private String key;
    ObjectNode payload = JsonNodeFactory.instance.objectNode();

    TicketCommentBuilder(JiraWebClientFactory jiraWebClientFactory, String key) {
        this.jiraWebClientFactory = jiraWebClientFactory;
        this.key = key;
    }

    public void withIsInternal() {
        ArrayNode properties = getPropertyNode();

        ObjectNode internalCommentProperty = JsonNodeFactory.instance.objectNode();
        internalCommentProperty.put("key", "sd.public.comment");

        ObjectNode internalCommentValue = JsonNodeFactory.instance.objectNode();
        internalCommentValue.put("internal", true);

        properties.add(internalCommentProperty);

        internalCommentProperty.set("value", internalCommentValue);
    }

    public void withComment(String comment) throws TicketException {
        if (isAtlassianDocumentFormat(comment)) {
            throw new TicketException("Attribute 'comment' must be in markdown format");
        }

        AtlassianDocumentMapper atlassianDocumentMapper = new AtlassianDocumentMapper();
        payload.set("body", atlassianDocumentMapper.fromMarkdown(comment));
    }

    public void post() throws TicketException {
        JsonNode result;
        try {
            result = this.jiraWebClientFactory.getWebClient().post()
                    .uri(uriBuilder -> uriBuilder.path("/rest/api/3/issue/" + key + "/comment").build())
                    .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).bodyValue(payload)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> response
                            .bodyToMono(String.class)
                            .flatMap(errorBody -> Mono.error(new TicketException(
                                    "Problems posting comment to ticket.  Bad request code: " + response.statusCode()
                                            + " body: " + errorBody + " payload:" + payload.toString()))))
                    .bodyToMono(JsonNode.class).block();
        } catch (Throwable exception) {
            if (exception.getCause() instanceof TicketException) {
                throw (TicketException) exception.getCause();
            }
            throw exception;
        }

        if (result == null || !result.has("id")) {
            throw new TicketException("Failed to add comment, unexpected response");
        }
    }

    private ArrayNode getPropertyNode() {
        if (!payload.has("properties")) {
            ArrayNode properties = JsonNodeFactory.instance.arrayNode();

            payload.set("properties", properties);
        }

        return (ArrayNode) payload.get("properties");
    }

    private static boolean isAtlassianDocumentFormat(String input) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(input);

            if (rootNode.has("type") && "doc".equals(rootNode.get("type").asText()) && rootNode.has("content")) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }
}
