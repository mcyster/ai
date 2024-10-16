package com.extole.jira.support;

import java.util.ArrayList;
import java.util.List;
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
public class SupportTicketService {
    private static int MAX_TICKETS_PER_REQUEST = 1024;
    private static int MAX_RETRIES = 5;
    
    private JiraWebClientFactory jiraWebClientFactory;
    
    SupportTicketService(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }
    
    public TicketQueryBuilder ticketQueryBuilder() {
        return new TicketQueryBuilder();
    }
    
    public class TicketQueryBuilder {
        Optional<Integer> limit = Optional.empty();
        String filter = "";
        
        public TicketQueryBuilder withTrailing7Months() {
            filter = " AND (created > startOfMonth(\"-7M\") OR resolved > startOfMonth(\"-7M\"))";
            return this;
        }

        public TicketQueryBuilder withTrailingWeek() {
            filter = " AND created >= -1w";
            return this;
        }

        public TicketQueryBuilder withTicket(String ticketNumber) {
            filter = " AND issueKey = " + ticketNumber;
            return this;
        }

        public TicketQueryBuilder withEpicsOnly() {
            filter = " AND type = Epic";
            return this;
        }
        
        public TicketQueryBuilder withLimit(Integer limit) {
            this.limit = Optional.of(limit);
            return this;
        }
        
        public List<FullSupportTicket> query() throws SupportTicketException {
            String query = "project in (\"HELP\", \"SUP\", \"LAUNCH\", \"SPEED\")"
                + filter
                + " ORDER BY CREATED ASC";
            
            return fetchFullTickets(query, limit);
        }
    }
    
    public Optional<FullSupportTicket> getTicket(String ticketNumber) throws SupportTicketException {
        List<FullSupportTicket> tickets = fetchFullTickets("issuekey = " + ticketNumber, Optional.empty());
        
        if (tickets.size() == 0) {
            return Optional.empty();
        }
        
        return Optional.of(tickets.get(0));
    }
    
    private List<FullSupportTicket> fetchFullTickets(String query, Optional<Integer> limit) throws SupportTicketException {

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
            add("customfield_11301");
            add("customfield_11302");
            add("customfield_11312");
            add("customfield_11326");
            add("resolutiondate");
            add("customfield_11375");
            add("customfield_11376");
            add("customfield_11373");
            add("customfield_11320");
            add("aggregatetimespent");
            add("description");
            add("comment");
        }};

         int maxResultsPerRequest = 100;
         if (limit.isPresent() && maxResultsPerRequest > limit.get()) {
             maxResultsPerRequest = limit.get();
         }
         int retries = 0;
         
         List<FullSupportTicket> tickets = new ArrayList<>();
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

                 tickets.addAll(issuesToFullTicketResponses((ArrayNode)issues));

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
                 retries += 1;
                 if (retries > MAX_RETRIES) {
                     throw new SupportTicketException("Last retry failed for jira query: " + query, exception);
                 }
             }
         } while (true);

         return tickets;
     }
        
    private static List<FullSupportTicket> issuesToFullTicketResponses(ArrayNode issues) {
        List<FullSupportTicket> tickets = new ArrayList<>();

        for(JsonNode issue: issues) {
            tickets.add(issueToFullTicketResponse(issue));
        }

        return tickets;      
    }
 
    private static FullSupportTicket issueToFullTicketResponse(JsonNode issue) {
        var ticketBuilder = FullSupportTicket.newBuilder();
        ticketBuilder.ticket(issueToTicketResponse(issue));
        
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
                    var commentBuilder = SupportTicketComment.newBuilder();
                    
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
       
    private static SupportTicket issueToTicketResponse(JsonNode issue) {
        JsonNode fields = issue.path("fields");

        String client = null;
        String value = fields.path("customfield_11312").path("value").asText();
        if (value != null && !value.trim().isEmpty()) {
            client = value.split("-")[0].trim();
        }

        var ticketBuilder = SupportTicket.newBuilder();
        ticketBuilder.key(issue.path("key").asText());
        ticketBuilder.project(fields.path("project").path("key").asText());
        ticketBuilder.created(fields.path("created").asText());
        ticketBuilder.type(fields.path("issuetype").path("name").asText());
        ticketBuilder.status(fields.path("status").path("name").asText());
        ticketBuilder.statusChanged(fields.path("statuscategorychangedate").asText());
        ticketBuilder.category(fields.path("parent").path("fields").path("summary").asText(null));
        ticketBuilder.resolved(fields.path("resolutiondate").asText(null));
        ticketBuilder.due(fields.path("customfield_11301").asText(null));
        ticketBuilder.start(fields.path("customfield_11302").asText(null));
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
}
