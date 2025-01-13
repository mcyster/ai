package com.cyster.jira.client.ticket;

import org.springframework.core.io.FileSystemResource;

public interface TicketCommentBuilder {

    TicketCommentBuilder withIsExternal();

    TicketCommentBuilder withAsset(String name, FileSystemResource resource);

    TicketCommentBuilder withComment(String comment) throws TicketException;

    void post() throws TicketException;

}
