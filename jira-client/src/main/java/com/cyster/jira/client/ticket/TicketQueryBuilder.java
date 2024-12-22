package com.cyster.jira.client.ticket;

import java.util.List;

public interface TicketQueryBuilder<TICKET> {

    TicketQueryBuilder<TICKET> withProject(String project);

    TicketQueryBuilder<TICKET> withProjects(List<String> projects);

    TicketQueryBuilder<TICKET> withLimit(Integer limit);

    TicketQueryBuilder<TICKET> addFilter(String filter);

    TicketQueryBuilder<TICKET> withOrder(String orderBy);

    List<TICKET> query() throws TicketException;

}
