package com.cyster.jira.client.ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.jira.client.web.JiraWebClientFactory;

@Component
public class TicketService {
    private JiraWebClientFactory jiraWebClientFactory;

    TicketService(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }

    public TicketQueryBuilder ticketQueryBuilder() {
        return new TicketQueryBuilder();
    }

    public Optional<Ticket> getTicket(String key) throws TicketException {
        List<Ticket> tickets = new ArrayList<>();

        if (tickets.size() == 0) {
            return Optional.empty();
        }

        return Optional.of(tickets.get(0));
    }

    public TicketCommentBuilder ticketCommentBuilder(String key) {
        return new TicketCommentBuilder(jiraWebClientFactory, key);
    }
}
