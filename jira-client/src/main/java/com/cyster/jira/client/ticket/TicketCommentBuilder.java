package com.cyster.jira.client.ticket;

public interface TicketCommentBuilder {

    TicketCommentBuilder withIsExternal();

    TicketCommentBuilder withComment(String comment) throws TicketException;

    void post() throws TicketException;

}
