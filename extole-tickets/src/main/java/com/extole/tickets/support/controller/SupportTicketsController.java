package com.extole.tickets.support.controller;


import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.jira.client.JiraWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@RestController
@RequestMapping("/support")
public class SupportTicketsController {    
    private JiraWebClientFactory jiraWebClientFactory;
    
    public SupportTicketsController(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }

    @GetMapping("/tickets")
    public List<SupportTicketResponse> getTickets() {

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
           //+ " AND created > startOfMonth(\"-6M\")"
           + " AND created >= -1w" 
           + " AND type in (Bug, Task, Story)"
           + " ORDER BY CREATED ASC";
        
        int maxResultsPerRequest = 50;  
        
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

    private static List<SupportTicketResponse> issuesToTicketResponses(ArrayNode issues) {
        List<SupportTicketResponse> tickets = new ArrayList<>();
        
        for(JsonNode issue: issues) {
            JsonNode fields = issue.path("fields");

            var ticketBuilder = SupportTicketResponse.newBuilder();            
            ticketBuilder.key(issue.path("key").asText());
            ticketBuilder.project(fields.path("project").path("key").asText());
            ticketBuilder.created(fields.path("created").asText());
            ticketBuilder.type(fields.path("issuetype").path("name").asText());
            ticketBuilder.status(fields.path("status").path("name").asText());
            ticketBuilder.statusChanged(fields.path("statuscategorychangedate").asText());
            ticketBuilder.category(fields.path("parent").path("fields").path("summary").asText());
            ticketBuilder.resolved(fields.path("resolutiondate").asText());
            ticketBuilder.due(fields.path("customfield_11301").asText());
            ticketBuilder.priority(fields.path("priority").path("name").asText());
            ticketBuilder.reporter(fields.path("reporter").path("emailAddress").asText());
            ticketBuilder.assignee(fields.path("assignee").path("emailAddress").asText());
            ticketBuilder.client(fields.path("customfield_11312").path("value").asText());  // TODO split [:-1] | join("-"))
            ticketBuilder.clientId(fields.path("customfield_11320").asText());  // TODO split [:-1] | join("-"))
            ticketBuilder.pod(fields.path("customfield_11326").asText());
            ticketBuilder.pairCsm(fields.path("customfield_11375").path("emailAddress").asText());
            ticketBuilder.pairSupport(fields.path("customfield_11376").path("emailAddress").asText());
            ticketBuilder.clientPriority(fields.path("customfield_11373").asText());
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
        String category,
        String resolved,
        String due,
        String priority,
        String reporter,
        String assignee,
        String client,
        String clientId,
        String pod,
        String pairCsm,
        String pairSupport,
        String clientPriority,
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
            private String category;
            private String resolved;
            private String due;
            private String priority;
            private String reporter;
            private String assignee;
            private String client;
            private String clientId;
            private String pod;
            private String pairCsm;
            private String pairSupport;
            private String clientPriority;
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
                this.category = category;
                return this;
            }

            public Builder resolved(String resolved) {
                this.resolved = resolved;
                return this;
            }

            public Builder due(String due) {
                this.due = due;
                return this;
            }

            public Builder priority(String priority) {
                this.priority = priority;
                return this;
            }

            public Builder reporter(String reporter) {
                this.reporter = reporter;
                return this;
            }

            public Builder assignee(String assignee) {
                this.assignee = assignee;
                return this;
            }

            public Builder client(String client) {
                this.client = client;
                return this;
            }

            public Builder clientId(String clientId) {
                this.clientId = clientId;
                return this;
            }

            public Builder pod(String pod) {
                this.pod = pod;
                return this;
            }

            public Builder pairCsm(String pairCsm) {
                this.pairCsm = pairCsm;
                return this;
            }

            public Builder pairSupport(String pairSupport) {
                this.pairSupport = pairSupport;
                return this;
            }

            public Builder clientPriority(String clientPriority) {
                this.clientPriority = clientPriority;
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

