package com.cyster.jira.client.ticket;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public interface TicketMapper {

    List<String> projects();

    List<String> fields();

    Ticket issueToTicket(JsonNode issue);
}
