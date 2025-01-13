package com.cyster.jira.client.ticket;

import java.net.URI;

import org.springframework.core.io.FileSystemResource;

public interface TicketAttachmentBuilder {

    TicketAttachmentBuilder withName(String name);

    TicketAttachmentBuilder withAsset(FileSystemResource resource);

    Attachment post() throws TicketException;

    record Attachment(String id, String filename, URI uri) {
    }
}
