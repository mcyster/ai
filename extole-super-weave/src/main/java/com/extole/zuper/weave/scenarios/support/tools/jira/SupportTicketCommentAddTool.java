package com.extole.zuper.weave.scenarios.support.tools.jira;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.ToolException;
import com.cyster.jira.client.ticket.TicketCommentBuilder.CommentId;
import com.cyster.jira.client.ticket.TicketException;
import com.extole.jira.support.SupportTicketService;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketCommentAddTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class SupportTicketCommentAddTool implements ExtoleSupportTool<Request> {
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
        return "Post a comment to a the specified ticket";
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
        if (request.key == null || request.key.isEmpty()) {
            throw new ToolException("Attribute 'key' not specified");
        }

        if (request.comment == null || request.comment.isEmpty()) {
            throw new ToolException("Attribute 'comment' must be specified");
        }

        CommentId commentId = null;
        try {
            commentId = supportTicketService.ticketCommentBuilder(request.key).withComment(request.comment).post();
        } catch (TicketException exception) {
            throw new ToolException("Error adding comment to ticket: " + request.key, exception);
        }

        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("key", request.key);
        response.put("commentId", commentId.id());

        return response;
    }

    public int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass());
    }

    static record Request(@JsonProperty(required = true) @JsonPropertyDescription("ticket key") String key,
            @JsonProperty(required = true) @JsonPropertyDescription("comment in Markdown format") String comment) {
    }

}
