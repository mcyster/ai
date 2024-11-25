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
    public TicketService createTicketService(TicketMapper ticketMapper) {
        return new TicketServiceImpl(jiraWebClientFactory, ticketMapper);
    }

    @Override
    public TicketService createTicketService() {
        return new TicketServiceImpl(jiraWebClientFactory, new DefaultTicketMapper());
    }

}
