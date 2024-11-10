package com.extole.zuper.weave.scenarios.support.tools.jira;

import java.util.Objects;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.jira.client.adf.writer.AtlassianDocumentMapper;
import com.extole.jira.support.SupportTicketException;
import com.extole.jira.support.SupportTicketService;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketCommentAddTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class SupportTicketCommentAddTool implements ExtoleSupportTool<Request> {
    private static final Logger logger = LoggerFactory.getLogger(SupportTicketCommentAddTool.class);

    private SupportTicketService supportTicketService;

    SupportTicketCommentAddTool(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
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
    public Object execute(Request request, Void context, OperationLogger operation) throws ToolException {
        if (request.key == null || request.key.isEmpty()) {
            throw new ToolException("Attribute 'key' not specified");
        }

        if (request.comment == null || request.comment.isEmpty()) {
            throw new ToolException("Attribute 'comment' must be specified");
        }

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            AtlassianDocumentMapper atlassianDocumentMapper = new AtlassianDocumentMapper();
            payload.set("body", atlassianDocumentMapper.fromMarkdown(request.comment));
            
            ArrayNode properties = JsonNodeFactory.instance.arrayNode();
            
            ObjectNode internalCommentProperty = JsonNodeFactory.instance.objectNode();
            internalCommentProperty.put("key", "sd.public.comment");
            
            ObjectNode internalCommentValue = JsonNodeFactory.instance.objectNode();
            internalCommentValue.put("internal", true);
            
            properties.add(internalCommentProperty);
            
            internalCommentProperty.set("value", internalCommentValue);
            
            payload.set("properties",  properties);       
        }

        try {
			supportTicketService.addComment(request.key, request.comment);
		} catch (SupportTicketException exception) {
			throw new ToolException("Error adding comment to ticket: " + request.key, exception);
		}

        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("key", request.key);
        return response;
    }

    public int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass());
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
