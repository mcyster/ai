package com.cyster.jira.client.ticket;

import java.util.Optional;

public interface TicketService<TICKET> {
    TicketQueryBuilder<TICKET> ticketQueryBuilder();

    Optional<TICKET> getTicket(String key) throws TicketException;

    TicketCommentBuilder ticketCommentBuilder(String key);

}