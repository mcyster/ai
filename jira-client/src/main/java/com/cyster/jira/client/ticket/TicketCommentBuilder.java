package com.cyster.jira.client.ticket;

public interface TicketCommentBuilder {

    TicketCommentBuilder withIsInternal();

    TicketCommentBuilder withComment(String comment) throws TicketException;

    void post() throws TicketException;

}
