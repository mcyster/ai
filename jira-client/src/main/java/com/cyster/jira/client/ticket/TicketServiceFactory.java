package com.cyster.jira.client.ticket;

import com.cyster.jira.client.ticket.impl.Ticket;

// More readable Ticket for AI
// Provide a simplified, more readable view of a ticket with well named attributes and leveraging markdown

public interface TicketServiceFactory {
    <TICKET> TicketService<TICKET> createTicketService(TicketMapper<TICKET> mapper);

    TicketService<Ticket> createTicketService();
}
