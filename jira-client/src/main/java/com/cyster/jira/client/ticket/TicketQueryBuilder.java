package com.cyster.jira.client.ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TicketQueryBuilder {
    Optional<Integer> limit = Optional.empty();
    List<String> projects = new ArrayList<>();

    String filter = "";

    public TicketQueryBuilder addProject(String project) {
        projects.add(project);
        return this;
    }

    public TicketQueryBuilder withLimit(Integer limit) {
        this.limit = Optional.of(limit);
        return this;
    }

    public List<Ticket> query() throws TicketException {
        String query = "project in (\"HELP\", \"SUP\", \"LAUNCH\", \"SPEED\")" + filter + " ORDER BY CREATED ASC";

        if (!projects.isEmpty()) {
            query = "project in ("
                    + projects.stream().map(project -> "\"" + project + "\"").collect(Collectors.joining(", ")) + ")"
                    + filter + " ORDER BY CREATED ASC";
        }

        return fetchTickets(query);
    }

    private List<Ticket> fetchTickets(String query) {
        return new ArrayList<>();
    }
}
