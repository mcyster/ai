package com.cyster.jira.client.ticket.impl;

import java.util.List;
import java.util.Optional;

import com.cyster.jira.client.ticket.TicketAttachmentBuilder;
import com.cyster.jira.client.ticket.TicketCommentBuilder;
import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.ticket.TicketMapper;
import com.cyster.jira.client.ticket.TicketQueryBuilder;
import com.cyster.jira.client.ticket.TicketService;
import com.cyster.jira.client.web.JiraWebClientFactory;

public class TicketServiceImpl<TICKET> implements TicketService<TICKET> {
    private final JiraWebClientFactory jiraWebClientFactory;
    private final TicketMapper<TICKET> ticketMapper;

    public TicketServiceImpl(JiraWebClientFactory jiraWebClientFactory, TicketMapper<TICKET> ticketMapper) {
        this.jiraWebClientFactory = jiraWebClientFactory;
        this.ticketMapper = ticketMapper;
    }

    public TicketQueryBuilder<TICKET> ticketQueryBuilder() {
        return new TicketQueryBuilderImpl<TICKET>(jiraWebClientFactory, ticketMapper);
    }

    public Optional<TICKET> getTicket(String key) throws TicketException {
        var ticketQueryBuilder = new TicketQueryBuilderImpl<TICKET>(jiraWebClientFactory, ticketMapper);

        ticketQueryBuilder.addFilter("key = \"" + key + "\"");

        List<TICKET> tickets = ticketQueryBuilder.query();

        if (tickets.size() == 0) {
            return Optional.empty();
        }

        return Optional.of(tickets.get(0));
    }

    @Override
    public TicketAttachmentBuilder ticketAttachmentBuilder(String key) {
        return new TicketAttachmentBuilderImpl(jiraWebClientFactory, key);
    }

    @Override
    public TicketCommentBuilder ticketCommentBuilder(String key) {
        return new TicketCommentBuilderImpl(jiraWebClientFactory, key);
    }

}
