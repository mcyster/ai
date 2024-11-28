package com.cyster.jira.client.ticket.impl;

import org.springframework.stereotype.Component;

import com.cyster.jira.client.ticket.TicketMapper;
import com.cyster.jira.client.ticket.TicketService;
import com.cyster.jira.client.ticket.TicketServiceFactory;
import com.cyster.jira.client.web.JiraWebClientFactory;

@Component
public class TicketServiceFactoryImpl implements TicketServiceFactory {
    private JiraWebClientFactory jiraWebClientFactory;

    TicketServiceFactoryImpl(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }

    @Override
    public <TICKET> TicketService<TICKET> createTicketService(TicketMapper<TICKET> ticketMapper) {
        return new TicketServiceImpl<TICKET>(jiraWebClientFactory, ticketMapper);
    }

    @Override
    public TicketService<Ticket> createTicketService() {
        return new TicketServiceImpl<Ticket>(jiraWebClientFactory, new DefaultTicketMapper());
    }

}
