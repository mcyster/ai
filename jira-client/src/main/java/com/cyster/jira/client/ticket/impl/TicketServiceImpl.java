package com.cyster.jira.client.ticket.impl;

import java.util.List;
import java.util.Optional;

import com.cyster.jira.client.ticket.Ticket;
import com.cyster.jira.client.ticket.TicketCommentBuilder;
import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.ticket.TicketMapper;
import com.cyster.jira.client.ticket.TicketQueryBuilder;
import com.cyster.jira.client.ticket.TicketService;
import com.cyster.jira.client.web.JiraWebClientFactory;

public class TicketServiceImpl implements TicketService {
    private final JiraWebClientFactory jiraWebClientFactory;
    private final TicketMapper ticketMapper;

    public TicketServiceImpl(JiraWebClientFactory jiraWebClientFactory, TicketMapper ticketMapper) {
        this.jiraWebClientFactory = jiraWebClientFactory;
        this.ticketMapper = ticketMapper;
    }

    public TicketQueryBuilder ticketQueryBuilder() {
        return new TicketQueryBuilderImpl(jiraWebClientFactory, ticketMapper);
    }

    public Optional<Ticket> getTicket(String key) throws TicketException {
        var ticketQueryBuilder = new TicketQueryBuilderImpl(jiraWebClientFactory, ticketMapper);

        ticketQueryBuilder.addFilter("key = \"" + key + "\"");

        List<Ticket> tickets = ticketQueryBuilder.query();

        if (tickets.size() == 0) {
            return Optional.empty();
        }

        return Optional.of(tickets.get(0));
    }

    public TicketCommentBuilder ticketCommentBuilder(String key) {
        return new TicketCommentBuilderImpl(jiraWebClientFactory, key);
    }

}
