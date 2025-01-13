package com.cyster.jira.client.ticket;

import java.io.InputStream;
import java.net.URI;

public interface TicketAttachmentBuilder {

    TicketAttachmentBuilder withAsset(String name, String type, InputStream inputStream);

    Attachment post() throws TicketException;

    record Attachment(String id, String filename, URI uri) {
    }
}
