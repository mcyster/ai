package com.extole.jira.engineering;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.jira.client.ticket.Ticket;
import com.cyster.jira.client.ticket.TicketCommentBuilder;
import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.ticket.TicketQueryBuilder;
import com.cyster.jira.client.ticket.TicketService;
import com.cyster.jira.client.ticket.TicketServiceFactory;
import com.cyster.jira.client.web.JiraWebClientFactory;

@Component
public class EngineeringTicketService implements TicketService {

    private final TicketService ticketService;

    EngineeringTicketService(TicketServiceFactory ticketServiceFactory, JiraWebClientFactory jiraWebClientFactory) {
        this.ticketService = ticketServiceFactory
                .createTicketService(new EngineeringTicketMapper(jiraWebClientFactory));
    }

    @Override
    public TicketQueryBuilder ticketQueryBuilder() {
        return ticketService.ticketQueryBuilder();
    }

    @Override
    public Optional<Ticket> getTicket(String key) throws TicketException {
        return ticketService.getTicket(key);
    }

    @Override
    public TicketCommentBuilder ticketCommentBuilder(String key) {
        return ticketService.ticketCommentBuilder(key);
    }

}
