package com.cyster.jira.client.ticket;

public interface TicketCommentBuilder {

    TicketCommentBuilder withIsExternal();

    TicketCommentBuilder withComment(String comment) throws TicketException;

    CommentId post() throws TicketException;

    public record CommentId(String id) {
    };
}
