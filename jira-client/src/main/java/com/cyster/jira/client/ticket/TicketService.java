package com.cyster.jira.client.ticket;

import java.util.Optional;

public interface TicketService {
    TicketQueryBuilder ticketQueryBuilder();

    Optional<Ticket> getTicket(String key) throws TicketException;

    TicketCommentBuilder ticketCommentBuilder(String key);

}