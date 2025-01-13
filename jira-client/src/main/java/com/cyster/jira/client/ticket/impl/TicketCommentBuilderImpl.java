package com.cyster.jira.client.ticket.impl;

import org.springframework.http.MediaType;

import com.cyster.jira.client.adf.writer.AtlassianDocumentMapper;
import com.cyster.jira.client.ticket.TicketCommentBuilder;
import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

public class TicketCommentBuilderImpl implements TicketCommentBuilder {
    private final JiraWebClientFactory jiraWebClientFactory;
    private final String key;
    private boolean isInternal = true;
    ObjectNode payload = JsonNodeFactory.instance.objectNode();

    TicketCommentBuilderImpl(JiraWebClientFactory jiraWebClientFactory, String key) {
        this.jiraWebClientFactory = jiraWebClientFactory;
        this.key = key;
    }

    public TicketCommentBuilder withIsExternal() {
        this.isInternal = false;
        return this;
    }

    public TicketCommentBuilder withComment(String comment) throws TicketException {
        if (isAtlassianDocumentFormat(comment)) {
            throw new TicketException("Attribute 'comment' must be in markdown format");
        }

        AtlassianDocumentMapper atlassianDocumentMapper = new AtlassianDocumentMapper();
        payload.set("body", atlassianDocumentMapper.fromMarkdown(comment));

        return this;
    }

    @Override
    public CommentId post() throws TicketException {
        if (isInternal) {
            withIsInternal();
        }

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

        return new CommentId(result.path("id").asText());
    }

    private TicketCommentBuilder withIsInternal() {
        ArrayNode properties = getPropertyNode();

        ObjectNode internalCommentProperty = JsonNodeFactory.instance.objectNode();
        internalCommentProperty.put("key", "sd.public.comment");

        ObjectNode internalCommentValue = JsonNodeFactory.instance.objectNode();
        internalCommentValue.put("internal", true);

        properties.add(internalCommentProperty);

        internalCommentProperty.set("value", internalCommentValue);

        return this;
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
        } catch (Exception exception) {
            return false;
        }

        return false;
    }

}
