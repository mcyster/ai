package com.extole.jira.support;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class SupportTicketClients {
    private final AtomicReference<Instant> lastUpdated = new AtomicReference<>();
    private final AtomicReference<Map<String, Client>> clients = new AtomicReference<>();

    private final JiraWebClientFactory jiraWebClientFactory;

    public SupportTicketClients(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }

    public Integer getJiraClientIndexForClientShortName(String clientShortName) throws TicketException {
        var clients = getClients();

        // clients.forEach((key, value) -> System.out.println(key + " -> " + value));

        if (!clients.containsKey(clientShortName)) {
            throw new TicketException("Client shortName not found: " + clientShortName);
        }
        return clients.get(clientShortName).id();
    }

    public String getClientShortNameForJiraClientIndex(Integer jiraClientIndex) throws TicketException {
        var clients = getClients();

        return clients.entrySet().stream().filter(entry -> entry.getValue().equals(jiraClientIndex))
                .map(Map.Entry::getKey).findFirst()
                .orElseThrow(() -> new TicketException("Client index not found: " + jiraClientIndex));
    }

    private Map<String, Client> getClients() throws TicketException {
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
        var lastRefresh = lastUpdated.get();
        return lastRefresh == null || ChronoUnit.HOURS.between(lastRefresh, Instant.now()) >= 1;
    }

    private Map<String, Client> loadClients() throws TicketException {
        var clients = new ArrayList<Client>();

        try {
            JsonNode result = this.jiraWebClientFactory.getWebClient().get()
                    .uri("/rest/api/3/issue/createmeta?expand=projects.issuetypes.fields")
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();

            if (result != null && result.has("projects")) {
                result.get("projects").forEach(project -> {
                    project.get("issuetypes").forEach(issueType -> {
                        if (issueType.has("fields") && issueType.get("fields").has("customfield_11312")) {
                            JsonNode allowedValues = issueType.get("fields").get("customfield_11312")
                                    .get("allowedValues");
                            if (allowedValues != null) {
                                allowedValues.forEach(value -> {
                                    String clientValue = value.get("value").asText();
                                    String[] clientParts = clientValue.split("-");

                                    if (clientParts.length == 2) {
                                        // this has problems as the names are not consistent
                                        String clientShortName = clientParts[0].trim().replaceAll("(?i)VIP.*$", "")
                                                .trim().toLowerCase();
                                        String clientId = clientParts[1].trim();

                                        int id = value.get("id").asInt();
                                        clients.add(new Client(clientShortName, clientId, id));
                                    }
                                });
                            }
                        }
                    });
                });
            }
        } catch (Throwable exception) {
            throw new TicketException("Unable to load client list", exception);
        }

        return clients.stream()
                .collect(Collectors.toMap(Client::shortName, client -> client, (existing, duplicate) -> existing));
    }

    record Client(String shortName, String clientId, Integer id) {
    };
}
