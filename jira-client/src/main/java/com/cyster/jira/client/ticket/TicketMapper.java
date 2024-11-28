package com.cyster.jira.client.ticket;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public interface TicketMapper<TICKET> {

    List<String> projects();

    List<String> fields();

    TICKET issueToTicket(JsonNode issue);
}
