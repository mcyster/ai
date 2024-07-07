package com.extole.tickets.support.controller;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.jira.client.JiraWebClientFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@RestController
@RequestMapping("/support")
public class SupportTicketsController {    
    private JiraWebClientFactory jiraWebClientFactory;
    private Path tempDirectory;
    private final ObjectMapper objectMapper;
    
    public SupportTicketsController(@Value("${AI_HOME}") String aiHome, JiraWebClientFactory jiraWebClientFactory, 
        ObjectMapper objectMapper) {
        Path directory = Paths.get(aiHome);
        if (!Files.exists(directory)) {
            throw new IllegalArgumentException("AI_HOME (" + aiHome + ") does not exist");
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("AI_HOME (" + aiHome + ") is not a directory");
        }
        this.tempDirectory = directory.resolve("tmp");
        if (!Files.isDirectory(tempDirectory)) {
            try {
                Files.createDirectories(tempDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Unable to create temp directory: " + tempDirectory.toString());
            }
        }

        this.jiraWebClientFactory = jiraWebClientFactory;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/tickets")
    public List<SupportTicketResponse> getTickets(@RequestParam Optional<Integer> limit) {       
        return loadTickets(limit);
    }
    
    @Scheduled(initialDelayString = "PT30M", fixedRateString = "PT1H")
    public void performScheduledTask() {
        loadTickets(Optional.empty());
    }
    
    private List<SupportTicketResponse> loadTickets(Optional<Integer> limit) {
        List<SupportTicketResponse> tickets;
        
        Path cacheFilename = getCacheFilename(getHash(limit));
        if (Files.exists(cacheFilename)) {
            String json;
            try {
                json = new String(Files.readAllBytes(cacheFilename));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            
            try {
                tickets = objectMapper.readValue(json, new TypeReference<List<SupportTicketResponse>>(){});
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            tickets = fetchTickets(limit);

            try (FileWriter file = new FileWriter(cacheFilename.toString())) {
                file.write(objectMapper.writeValueAsString(tickets));
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
        
        return tickets;
    }
    
    private List<SupportTicketResponse> fetchTickets(Optional<Integer> limit) {

       List<String> fields = new ArrayList<>() {{
           add("key");
           add("project");
           add("created");
           add("summary");
           add("parent");
           add("priority");
           add("issuetype");
           add("status");
           add("statuscategorychangedate");
           add("reporter");
           add("assignee");
           add("customfield_11301");
           add("customfield_11312");
           add("customfield_11326");
           add("resolutiondate");
           add("customfield_11375");
           add("customfield_11376");
           add("customfield_11373");
           add("customfield_11320");
           add("aggregatetimespent");
       }};
       
       String query = "project in (\"SUP\", \"LAUNCH\", \"SPEED\")"
           + " AND (created > startOfMonth(\"-7M\") OR resolved > startOfMonth(\"-7M\"))"
           //+ " AND created >= -1w" 
           + " AND type in (Bug, Task, Story)"
           + " ORDER BY CREATED ASC";
        
        int maxResultsPerRequest = 100;
        if (limit.isPresent() && maxResultsPerRequest > limit.get()) {
            maxResultsPerRequest = limit.get();
        }
        
        List<SupportTicketResponse> tickets = new ArrayList<>();
        do {     
            JsonNode response;
            try {
                int maxResults = maxResultsPerRequest;  
                response = this.jiraWebClientFactory.getWebClient().get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/3/search")
                        .queryParam("jql", query)
                        .queryParam("startAt", tickets.size())
                        .queryParam("maxResults", maxResults)
                        .queryParam("fields", fields)
                        .build())
                  .accept(MediaType.APPLICATION_JSON)
                  .retrieve()
                  .bodyToMono(JsonNode.class)
                  .block();
                
                if (response == null || !response.has("issues")) {
                    throw new IllegalArgumentException("Jira search failed with unexpected response");
                }
                var issues = response.path("issues");
                if (!issues.isArray()) {
                    throw new IllegalArgumentException("Jira search failed with unexpected response");
                }
                
                tickets.addAll(issuesToTicketResponses((ArrayNode)issues));
                
                if (limit.isPresent() && tickets.size() >= limit.get()) {
                    break;
                }
                
                if (tickets.size() >= response.path("total").asInt()) {
                    break;
                }
 
            } catch (WebClientResponseException exception) {
                if (exception.getCause() instanceof DataBufferLimitException)
                if (maxResultsPerRequest <= 1) {
                    throw new RuntimeException("Unable to fetch tickets in small enough chunks for size", exception);
                }
                maxResultsPerRequest = maxResultsPerRequest / 2;
                if (maxResultsPerRequest < 1) {
                    maxResultsPerRequest = 1;
                }
            }
        } while (true);    
      
        return tickets;
    }

    private Path getCacheFilename(String uniqueHash) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(new Date());
        
        return tempDirectory.resolve("tickets-" + uniqueHash + "-" + date + ".json");
    }
    
    public static String getHash(Object... parameters) {
        StringBuilder concatenated = new StringBuilder();
        for (Object parameter : parameters) {
            concatenated.append(convertToString(parameter));
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException("Unable to find digest", exception);
        }
        byte[] hashBytes = digest.digest(concatenated.toString().getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private static String convertToString(Object parameter) {
        if (parameter instanceof Optional) {
            Optional<?> optionalParam = (Optional<?>) parameter;
            return optionalParam.map(Object::toString).orElse("");
        } else {
            return parameter.toString();
        }
    }
    
    private static List<SupportTicketResponse> issuesToTicketResponses(ArrayNode issues) {
        List<SupportTicketResponse> tickets = new ArrayList<>();
        
        for(JsonNode issue: issues) {
            JsonNode fields = issue.path("fields");

            String client = null;
            String value = fields.path("customfield_11312").path("value").asText();
            if (value != null && !value.trim().isEmpty()) {
                client = value.split("-")[0].trim();
            }
            
            var ticketBuilder = SupportTicketResponse.newBuilder();            
            ticketBuilder.key(issue.path("key").asText());
            ticketBuilder.project(fields.path("project").path("key").asText());
            ticketBuilder.created(fields.path("created").asText());
            ticketBuilder.type(fields.path("issuetype").path("name").asText());
            ticketBuilder.status(fields.path("status").path("name").asText());
            ticketBuilder.statusChanged(fields.path("statuscategorychangedate").asText());
            ticketBuilder.category(fields.path("parent").path("fields").path("summary").asText(null));
            ticketBuilder.resolved(fields.path("resolutiondate").asText(null));
            ticketBuilder.due(fields.path("customfield_11301").asText(null));
            ticketBuilder.priority(fields.path("priority").path("name").asText());
            ticketBuilder.reporter(fields.path("reporter").path("emailAddress").asText(null));
            ticketBuilder.assignee(fields.path("assignee").path("emailAddress").asText(null));
            ticketBuilder.client(client);
            ticketBuilder.clientId(fields.path("customfield_11320").asText(null));
            ticketBuilder.pod(fields.path("customfield_11326").asText(null));
            ticketBuilder.pairCsm(fields.path("customfield_11375").path("emailAddress").asText(null));
            ticketBuilder.pairSupport(fields.path("customfield_11376").path("emailAddress").asText(null));
            ticketBuilder.clientPriority(fields.path("customfield_11373").asText(null));
            ticketBuilder.timeSeconds(fields.path("aggregatetimespent").asInt());
            
            tickets.add(ticketBuilder.build());
        }
        
        return tickets;
    }
    
    public static record SupportTicketResponse (
            String key,
            String project,
            String created,
            String type,
            String status,
            String statusChanged,
            Optional<String> category,
            Optional<String> resolved,
            Optional<String> due,
            String priority,
            Optional<String> reporter,
            Optional<String> assignee,
            Optional<String> client,
            Optional<String> clientId,
            Optional<String> pod,
            Optional<String> pairCsm,
            Optional<String> pairSupport,
            Optional<String> clientPriority,
            Integer timeSeconds
    ) {
        public static Builder newBuilder() {
            return new Builder();
        }

        public static class Builder {
            private String key;
            private String project;
            private String created;
            private String type;
            private String status;
            private String statusChanged;
            private Optional<String> category = Optional.empty();
            private Optional<String> resolved = Optional.empty();
            private Optional<String> due = Optional.empty();
            private String priority;
            private Optional<String> reporter = Optional.empty();
            private Optional<String> assignee = Optional.empty();
            private Optional<String> client = Optional.empty();
            private Optional<String> clientId = Optional.empty();
            private Optional<String> pod = Optional.empty();
            private Optional<String> pairCsm = Optional.empty();
            private Optional<String> pairSupport = Optional.empty();
            private Optional<String> clientPriority = Optional.empty();
            private Integer timeSeconds;

            public Builder key(String key) {
                this.key = key;
                return this;
            }

            public Builder project(String project) {
                this.project = project;
                return this;
            }

            public Builder created(String created) {
                this.created = created;
                return this;
            }

            public Builder type(String type) {
                this.type = type;
                return this;
            }

            public Builder status(String status) {
                this.status = status;
                return this;
            }

            public Builder statusChanged(String statusChanged) {
                this.statusChanged = statusChanged;
                return this;
            }

            public Builder category(String category) {
                this.category = Optional.ofNullable(category);
                return this;
            }

            public Builder resolved(String resolved) {
                this.resolved = Optional.ofNullable(resolved);
                return this;
            }

            public Builder due(String due) {
                this.due = Optional.ofNullable(due);
                return this;
            }

            public Builder priority(String priority) {
                this.priority = priority;
                return this;
            }

            public Builder reporter(String reporter) {
                this.reporter = Optional.ofNullable(reporter);
                return this;
            }

            public Builder assignee(String assignee) {
                this.assignee = Optional.ofNullable(assignee);
                return this;
            }

            public Builder client(String client) {
                this.client = Optional.ofNullable(client);
                return this;
            }

            public Builder clientId(String clientId) {
                this.clientId = Optional.ofNullable(clientId);
                return this;
            }

            public Builder pod(String pod) {
                this.pod = Optional.ofNullable(pod);
                return this;
            }

            public Builder pairCsm(String pairCsm) {
                this.pairCsm = Optional.ofNullable(pairCsm);
                return this;
            }

            public Builder pairSupport(String pairSupport) {
                this.pairSupport = Optional.ofNullable(pairSupport);
                return this;
            }

            public Builder clientPriority(String clientPriority) {
                this.clientPriority = Optional.ofNullable(clientPriority);
                return this;
            }

            public Builder timeSeconds(Integer timeSeconds) {
                this.timeSeconds = timeSeconds;
                return this;
            }

            public SupportTicketResponse build() {
                return new SupportTicketResponse(
                    key, project, created, type, status, statusChanged, category, resolved, due, 
                    priority, reporter, assignee, client, clientId, pod, pairCsm, pairSupport, 
                    clientPriority, timeSeconds
                );
            }
        }
    }

}

