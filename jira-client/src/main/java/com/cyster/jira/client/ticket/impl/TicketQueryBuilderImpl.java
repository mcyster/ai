package com.cyster.jira.client.ticket.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.ticket.TicketMapper;
import com.cyster.jira.client.ticket.TicketQueryBuilder;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;

public class TicketQueryBuilderImpl<TICKET> implements TicketQueryBuilder<TICKET> {
    private static final int MAX_TICKETS_PER_REQUEST = 1024;
    private static final int MAX_RETRIES = 5;
    private static final Logger logger = LoggerFactory.getLogger(TicketQueryBuilder.class);

    private final JiraWebClientFactory jiraWebClientFactory;
    private final TicketMapper<TICKET> ticketMapper;
    private Optional<Integer> limit = Optional.empty();
    private String filter = "type != Epic";
    private String order = "ORDER BY created ASC";

    public TicketQueryBuilderImpl(JiraWebClientFactory jiraWebClientFactory, TicketMapper<TICKET> ticketMapper) {
        this.jiraWebClientFactory = jiraWebClientFactory;
        this.ticketMapper = ticketMapper;

        this.withProjects(ticketMapper.projects());
    }

    public TicketQueryBuilderImpl<TICKET> withProject(String project) {
        addFilter("project = " + project);
        return this;
    }

    public TicketQueryBuilderImpl<TICKET> withProjects(List<String> projects) {
        if (!projects.isEmpty()) {
            addFilter("project in ( "
                    + projects.stream().map(project -> "\"" + project + "\"").collect(Collectors.joining(", ")) + " )");
        }
        return this;
    }

    public TicketQueryBuilderImpl<TICKET> withLimit(Integer limit) {
        this.limit = Optional.of(limit);

        return this;
    }

    public TicketQueryBuilderImpl<TICKET> addFilter(String filter) {
        if (this.filter.isEmpty()) {
            this.filter = filter;
        } else {
            this.filter = this.filter + " AND " + filter;
        }

        return this;
    }

    public TicketQueryBuilderImpl<TICKET> withOrder(String orderBy) {
        order = orderBy;
        return this;
    }

    public List<TICKET> query() throws TicketException {
        String query = filter + " " + order;

        int maxResultsPerRequest = 100;
        if (limit.isPresent() && maxResultsPerRequest > limit.get()) {
            maxResultsPerRequest = limit.get();
        }
        int retries = 0;

        logger.info("Jira query: " + query);

        List<TICKET> tickets = new ArrayList<>();
        do {
            JsonNode response;
            try {
                int maxResults = maxResultsPerRequest;
                response = this.jiraWebClientFactory.getWebClient().get().uri(uriBuilder -> {
                    var uri = uriBuilder.path("/rest/api/3/search").queryParam("jql", query)
                            .queryParam("startAt", tickets.size()).queryParam("maxResults", maxResults);
                    if (!ticketMapper.fields().isEmpty()) {
                        uri.queryParam("fields", ticketMapper.fields());
                    }
                    return uri.build();
                }).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();

                if (response == null || !response.has("issues")) {
                    throw new IllegalArgumentException("Jira search failed with unexpected response");
                }
                var issues = response.path("issues");
                if (!issues.isArray()) {
                    throw new IllegalArgumentException("Jira search failed with unexpected response");
                }

                for (var issue : issues) {
                    tickets.add(ticketMapper.issueToTicket((JsonNode) issue));
                }

                if (limit.isPresent() && tickets.size() >= limit.get()) {
                    break;
                }

                if (tickets.size() >= response.path("total").asInt()) {
                    break;
                }

                maxResultsPerRequest = maxResultsPerRequest * 2;
                if (maxResultsPerRequest > MAX_TICKETS_PER_REQUEST) {
                    maxResultsPerRequest = MAX_TICKETS_PER_REQUEST;
                }
            } catch (WebClientResponseException exception) {
                if (exception.getCause() instanceof DataBufferLimitException) {
                    if (maxResultsPerRequest <= 1) {
                        throw new RuntimeException("Unable to fetch tickets in small enough chunks for size",
                                exception);
                    }

                    maxResultsPerRequest = maxResultsPerRequest / 2;
                    if (maxResultsPerRequest < 1) {
                        maxResultsPerRequest = 1;
                    }
                }
                retries += 1;
                if (retries > MAX_RETRIES) {
                    throw new TicketException("Last retry failed for jira query: " + query, exception);
                }
            }
        } while (true);

        return tickets;
    }

}
