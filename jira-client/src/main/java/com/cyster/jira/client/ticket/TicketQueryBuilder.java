package com.cyster.jira.client.ticket;

import java.util.List;

import com.cyster.jira.client.ticket.impl.TicketQueryBuilderImpl;

public interface TicketQueryBuilder {

    TicketQueryBuilderImpl withProject(String project);

    TicketQueryBuilderImpl withProjects(List<String> projects);

    TicketQueryBuilderImpl withLimit(Integer limit);

    TicketQueryBuilderImpl addFilter(String filter);

    TicketQueryBuilderImpl withOrder(String orderBy);

    List<Ticket> query() throws TicketException;

}
