package com.extole.jira.engineering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.jira.client.adf.reader.MarkDownDocumentMapper;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
public class EngineeringTicketService {
    private static int MAX_TICKETS_PER_REQUEST = 1024;
    
    private JiraWebClientFactory jiraWebClientFactory;
    
    EngineeringTicketService(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }
    
    public TicketQueryBuilder ticketQueryBuilder() {
        return new TicketQueryBuilder();
    }
    
    public class TicketQueryBuilder {
        Optional<Integer> limit = Optional.empty();
        String filter = "created >= -1w";
        
        public TicketQueryBuilder withTrailing7Months() {
            filter = "(created > startOfMonth(\"-7M\") OR resolved > startOfMonth(\"-7M\"))";
            return this;
        }

        public TicketQueryBuilder withTrailingWeek() {
            filter = "created >= -1w";
            return this;
        }

        public TicketQueryBuilder withTicket(String ticketNumber) {
            filter = "issueKey = " + ticketNumber;
            return this;
        }
        
        public TicketQueryBuilder withLimit(Integer limit) {
            this.limit = Optional.of(limit);
            return this;
        }
        
        public List<FullEngineeringTicket> query() {
            String query = "project in (\"ENG\", \"T3\")"
                + " AND " + filter
                + " AND type in (Bug, Task, Story)"
                + " ORDER BY CREATED ASC";
            
            return fetchFullTickets(query, limit);
        }
    }
    
    public Optional<FullEngineeringTicket> getTicket(String ticketNumber) {
        List<FullEngineeringTicket> tickets = fetchFullTickets("issuekey = " + ticketNumber, Optional.empty());
        
        if (tickets.size() == 0) {
            return Optional.empty();
        }
        
        return Optional.of(tickets.get(0));
    }
    
    private Map<String, String> fetchInitiatives() {
        List<String> fields = new ArrayList<>() {{
            add("key");
            add("summary");
        }};
        
        String query = "project in (ENG, T3) and issuetype = Initiative ORDER BY created DESC";
        
        JsonNode response;
        try {
            response = this.jiraWebClientFactory.getWebClient().get()
                .uri(uriBuilder -> uriBuilder
                    .path("/rest/api/3/search")
                    .queryParam("jql", query)
                    .queryParam("fields", fields)
                    .build())
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .bodyToMono(JsonNode.class)
              .block();
        } catch (WebClientResponseException exception) {
            throw new RuntimeException("Unable to engineering initiatives", exception);
        }
        if (response == null || !response.has("issues")) {
            throw new IllegalArgumentException("Jira search failed with unexpected response while fetching initatives");
        }
        var issues = response.path("issues");
        if (!issues.isArray()) {
            throw new IllegalArgumentException("Jira search failed with unexpected response");
        } 
        
        Map<String, String> initiatives = new HashMap<>();
        for(JsonNode issue: issues) {
            JsonNode keyNode = issue.path("key");
            JsonNode summaryNode = issue.path("fields").path("summary");
            initiatives.put(keyNode.asText(), summaryNode.asText());
        }
        return initiatives;
    }
    
    private Map<String, Epic> fetchEpics() {
        
        Map<String, String> initatives = fetchInitiatives();
        
        List<String> fields = new ArrayList<>() {{
            add("key");
            add("summary");
            add("parent");
        }};
        
        String query = "project in (ENG, T3) and issuetype = Epic ORDER BY created DESC";
        
        Map<String, Epic> epics = new HashMap<>();
        do {
            JsonNode response;
            try {
                response = this.jiraWebClientFactory.getWebClient().get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/3/search")
                        .queryParam("jql", query)
                        .queryParam("startAt", epics.size())
                        .queryParam("maxResults", 100)
                        .queryParam("fields", fields)
                        .build())
                  .accept(MediaType.APPLICATION_JSON)
                  .retrieve()
                  .bodyToMono(JsonNode.class)
                  .block();
            } catch (WebClientResponseException exception) {
                throw new RuntimeException("Unable to fetch engineering epics", exception);
            }
            if (response == null || !response.has("issues")) {
                throw new IllegalArgumentException("Jira search failed with unexpected response while fetching initatives");
            }
            
            var issues = response.path("issues");
            if (!issues.isArray()) {
                throw new IllegalArgumentException("Jira search failed with unexpected response");
            } 

        
            for(JsonNode issue: issues) {
                JsonNode keyNode = issue.path("key");
                JsonNode summaryNode = issue.path("fields").path("summary");
                String initativeKey = issue.path("fields").path("parent").path("key").asText();
                
                Optional<String> initiative = Optional.empty();
                if (initativeKey != null && !initativeKey.isBlank() && initatives.containsKey(initativeKey)) {
                    initiative = Optional.of(initatives.get(initativeKey));
                }
                
                epics.put(keyNode.asText(), new Epic(keyNode.asText(), summaryNode.asText(), initiative));
            }
            
            if (epics.keySet().size() >= response.path("total").asInt()) {
                break;
            }
        } while(true);
        
        return epics;
    }
    
    private List<FullEngineeringTicket> fetchFullTickets(String query, Optional<Integer> limit) {

        var epics = fetchEpics();
        
        List<String> fields = new ArrayList<>() {{
            add("key");
            add("project");
            add("created");
            add("summary");
            add("parent");
            add("priority");
            add("issuetype");
            add("status");
            add("labels");
            add("statuscategorychangedate");
            add("reporter");
            add("assignee");
            add("resolutiondate");
            add("customfield_10800");  // Team
            add("description");
            add("comment");
        }};

         int maxResultsPerRequest = 100;
         if (limit.isPresent() && maxResultsPerRequest > limit.get()) {
             maxResultsPerRequest = limit.get();
         }

         List<FullEngineeringTicket> tickets = new ArrayList<>();
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

                 tickets.addAll(issuesToFullTicketResponses(epics, (ArrayNode)issues));

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
                         throw new RuntimeException("Unable to fetch tickets in small enough chunks for size", exception);
                     }
                     
                     maxResultsPerRequest = maxResultsPerRequest / 2;
                     if (maxResultsPerRequest < 1) {
                         maxResultsPerRequest = 1;
                     }
                 }
             }
             
         } while (true);

         return tickets;
     }
        
    private static List<FullEngineeringTicket> issuesToFullTicketResponses(Map<String, Epic> epics, ArrayNode issues) {
        List<FullEngineeringTicket> tickets = new ArrayList<>();

        for(JsonNode issue: issues) {
            tickets.add(issueToFullTicketResponse(epics, issue));
        }

        return tickets;      
    }
 
    private static FullEngineeringTicket issueToFullTicketResponse(Map<String, Epic> epics, JsonNode issue) {
        var ticketBuilder = FullEngineeringTicket.newBuilder();
        ticketBuilder.ticket(issueToTicketResponse(epics, issue));
        
        JsonNode descriptionNode = issue.path("fields").path("description");
        if (descriptionNode.isNull() || descriptionNode.isMissingNode()) {
            ticketBuilder.description("");
        } else {                            
            String content = new MarkDownDocumentMapper().fromAtlassianDocumentFormat(descriptionNode);
            ticketBuilder.description(content);
        }

        JsonNode commentContainerNode = issue.path("fields").path("comment");
        if (!commentContainerNode.isMissingNode()) {
            if (!commentContainerNode.path("comments").isMissingNode()) {            
                ArrayNode commentsNode = (ArrayNode)commentContainerNode.path("comments");
                for(JsonNode commentNode: commentsNode) {
                    var commentBuilder = EngineeringTicketComment.newBuilder();
                    
                    String author = commentNode.path("author").path("emailAddress").asText();
                    if (author == null || author.isBlank()) {
                        author = "support@extole.com";
                    }
                    commentBuilder.author(author);
                   
                    JsonNode bodyNode = commentNode.path("body");
                    if (!bodyNode.isMissingNode()) {             
                        commentBuilder.description(new MarkDownDocumentMapper().fromAtlassianDocumentFormat(bodyNode));
                    }
     
                    commentBuilder.created(commentNode.path("created").asText());
                    
                    ticketBuilder.addComment(commentBuilder.build());
                }
            }
        }
        
        return ticketBuilder.build();   
    }
    
    private static EngineeringTicket issueToTicketResponse(Map<String, Epic> epics, JsonNode issue) {
        JsonNode fields = issue.path("fields");
        
        Optional<String> initative = Optional.empty();
        String epicKey = fields.path("parent").path("key").asText();
        if (epicKey != null && !epicKey.isBlank() && epics.containsKey(epicKey)) {
            initative = epics.get(epicKey).initiative();
        }
        
        var ticketBuilder = EngineeringTicket.newBuilder();
        ticketBuilder.key(issue.path("key").asText());
        ticketBuilder.project(fields.path("project").path("key").asText());
        ticketBuilder.type(fields.path("issuetype").path("name").asText());
        ticketBuilder.status(fields.path("status").path("name").asText());
        ticketBuilder.statusChanged(fields.path("statuscategorychangedate").asText());
        ticketBuilder.epic(fields.path("parent").path("fields").path("summary").asText(null));
        if (initative.isPresent()) {
            ticketBuilder.initiative(initative.get());
        }
        ticketBuilder.created(fields.path("created").asText());
        ticketBuilder.resolved(fields.path("resolutiondate").asText(null));
        ticketBuilder.priority(fields.path("priority").path("name").asText());
        ticketBuilder.reporter(fields.path("reporter").path("emailAddress").asText(null));
        ticketBuilder.assignee(fields.path("assignee").path("emailAddress").asText(null));
        ticketBuilder.team(fields.path("customfield_10800").path("name").asText());
        ticketBuilder.summary(fields.path("summary").asText());
        
        var labelNode = fields.path("labels");
        if (labelNode.isArray()) {
            var labels = new ArrayList<String>();
            ArrayNode labelsNode = (ArrayNode) labelNode;
            for (JsonNode node : labelsNode) {
                String label = node.asText();
                labels.add(label);
            }
            ticketBuilder.labels(labels);
        }
        
        return ticketBuilder.build();
    }
    
    private static record Epic(String key, String epic, Optional<String> initiative) {};
    
}
