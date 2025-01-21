package com.extole.jira.support;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.client.web.ExtoleWebClientException;
import com.fasterxml.jackson.databind.JsonNode;

@Component
class SupportTicketClients {
    private final AtomicReference<Instant> lastUpdated = new AtomicReference<>();
    private final AtomicReference<Map<String, Client>> clients = new AtomicReference<>();

    private final JiraWebClientFactory jiraWebClientFactory;
    private final ExtoleTrustedWebClientFactory extoleTrustedWebClientFactory;

    public SupportTicketClients(JiraWebClientFactory jiraWebClientFactory,
            ExtoleTrustedWebClientFactory extoleTrustedWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
        this.extoleTrustedWebClientFactory = extoleTrustedWebClientFactory;
    }

    public Integer getJiraClientIndexForClientShortName(String clientShortName) throws TicketException {
        var clients = getClients();

        if (!clients.containsKey(clientShortName)) {
            System.out.println("Search failed for: " + clientShortName);
            clients.forEach((key, value) -> System.out.println(key + " -> " + value));

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

        var clientShortNames = loadClientShortNames();

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
                                        String clientId = clientParts[1].trim();

                                        int id = value.get("id").asInt();

                                        String shortName;
                                        if (clientShortNames.containsKey(clientId)) {
                                            shortName = clientShortNames.get(clientId);
                                        } else {
                                            shortName = clientParts[0].trim().replaceAll("(?i)VIP.*$", "").trim()
                                                    .toLowerCase();
                                        }
                                        clients.add(new Client(shortName, clientId, id));
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

    private Map<String, String> loadClientShortNames() throws TicketException {
        JsonNode resultNode;
        try {
            resultNode = this.extoleTrustedWebClientFactory.getSuperUserWebClient().get()
                    .uri(uriBuilder -> uriBuilder.path("/v4/clients").queryParam("type=CUSTOMER").build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
        } catch (ExtoleWebClientException | WebClientResponseException.Forbidden exception) {
            throw new TicketException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new TicketException("Internal error, unable to get clients");
        }

        if (!resultNode.isArray()) {
            throw new TicketException("Unable to get clients from extole!");
        }

        Map<String, String> clientShortNames = new HashMap<>();
        for (JsonNode node : resultNode) {
            if ("CUSTOMER".equals(node.path("client_type").asText())) {
                String clientId = node.path("client_id").asText();
                String shortName = node.path("short_name").asText();
                clientShortNames.put(clientId, shortName);
            }
        }
        return clientShortNames;
    }

    record Client(String shortName, String clientId, Integer id) {
    };
}
