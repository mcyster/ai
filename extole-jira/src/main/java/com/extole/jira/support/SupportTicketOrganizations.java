package com.extole.jira.support;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class SupportTicketOrganizations {
    private AtomicReference<Instant> lastUpdated = new AtomicReference<>();;
    private AtomicReference<Map<String, Integer>> clients = new AtomicReference<>();;

    private JiraWebClientFactory jiraWebClientFactory;

    public SupportTicketOrganizations(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }

    public Integer getJiraOrganizationIndexFromClientShortName(String clientShortName) throws TicketException {
        var clients = getClients();
        if (!clients.containsKey(clientShortName)) {
            throw new TicketException("Client shortName not found: " + clientShortName);
        }
        return clients.get(clientShortName);
    }

    public String getClientShortNameForJiraOrganizationIndex(Integer organizationIndex) throws TicketException {
        var clients = getClients();

        return clients.entrySet().stream().filter(entry -> entry.getValue().equals(organizationIndex))
                .map(Map.Entry::getKey).findFirst()
                .orElseThrow(() -> new TicketException("Organization index not found: " + organizationIndex));
    }

    private Map<String, Integer> getClients() throws TicketException {
        if (needsRefresh()) {
            refresh();
        }
        return clients.get();
    }

    private void refresh() throws TicketException {
        if (needsRefresh()) {
            clients.set(loadClients());
            lastUpdated.set(Instant.now());
        }
    }

    private boolean needsRefresh() {
        if (lastUpdated.get() == null) {
            return true;
        }

        return ChronoUnit.HOURS.between(lastUpdated.get(), Instant.now()) >= 1;
    }

    private Map<String, Integer> loadClients() throws TicketException {
        var organizations = new HashMap<String, Integer>();

        int offset = 0;
        final int limit = 50;

        try {
            while (true) {
                final int start = offset;
                JsonNode result = this.jiraWebClientFactory.getWebClient().get()
                        .uri(uriBuilder -> uriBuilder.path("/rest/servicedeskapi/organization")
                                .queryParam("start", start).queryParam("limit", limit).build())
                        .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();

                if (result != null && result.has("values")) {
                    for (JsonNode organizationNode : result.get("values")) {
                        System.out.println("ZZZZZZZZZZZZZZ " + organizationNode.toPrettyString());
                        String name = organizationNode.get("name").asText();
                        int id = organizationNode.get("id").asInt();
                        organizations.put(name, id);
                    }

                    if (result.get("isLastPage").asBoolean()) {
                        break;
                    }

                    offset += limit;
                } else {
                    break;
                }
            }
        } catch (Throwable exception) {
            throw new TicketException("Unable to load client list", exception);
        }

        return organizations;
    }
}
