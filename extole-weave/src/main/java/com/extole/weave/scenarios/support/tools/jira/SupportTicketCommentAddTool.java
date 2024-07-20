package com.extole.weave.scenarios.support.tools.jira;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.ToolException;
import com.cyster.jira.client.adf.writer.AtlassianDocumentMapper;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.extole.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.weave.scenarios.support.tools.jira.SupportTicketCommentAddTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@Component
class SupportTicketCommentAddTool implements ExtoleSupportTool<Request> {
    private static final Logger logger = LogManager.getLogger(SupportTicketCommentAddTool.class);

    private JiraWebClientFactory jiraWebClientFactory;
    private final boolean testMode;

    SupportTicketCommentAddTool(JiraWebClientFactory jiraWebClientFactory,
        @Value("${JIRA_TEST_MODE:false}") boolean testMode) {
        this.jiraWebClientFactory = jiraWebClientFactory;
        this.testMode = testMode;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Post a comment to a the support ticket system";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        if (request.key == null || request.key.isEmpty()) {
            throw new ToolException("Attribute 'key' not specified");
        }

        if (request.comment == null || request.comment.isEmpty()) {
            throw new ToolException("Attribute 'comment' must be specified");
        }

        if (isAtlassianDocumentFormat(request.comment)) {
            throw new ToolException("Attribute 'comment' must be in markdown format");
        }

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            AtlassianDocumentMapper atlassianDocumentMapper = new AtlassianDocumentMapper();
            payload.set("body", atlassianDocumentMapper.fromMarkdown(request.comment));
        }

        if (testMode) {
            logger.info("Jira (test mode) add comment" + payload.toString());
            return JsonNodeFactory.instance.objectNode().put("status", "success");
        }

        JsonNode result;
        try {
            result = this.jiraWebClientFactory.getWebClient().post()
            .uri(uriBuilder -> uriBuilder.path("/rest/api/3/issue/" + request.key + "/comment").build())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                response.bodyToMono(String.class).flatMap(errorBody ->
                    Mono.error(new ToolException("Problems posting comment to ticket", "Bad request code: " + response.statusCode() + " body: " + errorBody + " payload:" + payload.toString()))
                )
            )
            .bodyToMono(JsonNode.class)
            .block();
        } catch(Throwable exception) {
            if (exception.getCause() instanceof ToolException) {
                throw (ToolException)exception.getCause();
            }
            throw exception;
        }

        if (result == null || !result.has("id")) {
            throw new ToolException("Failed to add comment, unexpected response");
        }

        return result;
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
    static class Request {
        @JsonPropertyDescription("ticket key")
        @JsonProperty(required = true)
        public String key;

        @JsonPropertyDescription("comment in Markdown format")
        @JsonProperty(required = true)
        public String comment;
    }
}
