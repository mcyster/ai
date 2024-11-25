package com.cyster.jira.client.ticket;

// More readable Ticket for AI
// Provide a simplified, more readable view of a ticket with well named attributes and leveraging markdown

public interface TicketServiceFactory {
    TicketService createTicketService(TicketMapper mapper);

    TicketService createTicketService();
}
