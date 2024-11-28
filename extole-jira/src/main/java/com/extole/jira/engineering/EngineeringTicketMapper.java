package com.extole.jira.engineering;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.jira.client.adf.reader.MarkDownDocumentMapper;
import com.cyster.jira.client.ticket.TicketMapper;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

class EngineeringTicketMapper implements TicketMapper<EngineeringTicket> {
    private static final DateTimeFormatter ZONED_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private Map<String, Epic> epics;
    private final List<String> fields = new ArrayList<>() {
        {
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
            add("customfield_10800"); // Team
            add("description");
            add("comment");
        }
    };

    private final List<String> projects = List.of("T3", "ENG");

    EngineeringTicketMapper(JiraWebClientFactory jiraWebClientFactory) {
        this.epics = fetchEpics(jiraWebClientFactory);
    }

    public List<String> projects() {
        return projects;
    }

    public List<String> fields() {
        return fields;
    }

    public EngineeringTicket issueToTicket(JsonNode issue) {
        var ticketBuilder = EngineeringTicket.builder();

        ticketBuilder.key(issue.path("key").asText());
        ticketBuilder.project(issue.path("project").path("name").asText());

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
                ArrayNode commentsNode = (ArrayNode) commentContainerNode.path("comments");
                for (JsonNode commentNode : commentsNode) {
                    String author = commentNode.path("author").path("emailAddress").asText("unknown@example.com");

                    String description = "";
                    JsonNode bodyNode = commentNode.path("body");
                    if (!bodyNode.isMissingNode()) {
                        description = new MarkDownDocumentMapper().fromAtlassianDocumentFormat(bodyNode);
                    }

                    Instant createdDate = toInstant(commentNode.path("created").asText(null));

                    ticketBuilder.addComment(description, author, createdDate);
                }
            }
        }

        JsonNode fields = issue.path("fields");

        ticketBuilder.project(fields.path("project").path("key").asText());
        ticketBuilder.summary(fields.path("summary").asText());

        ticketBuilder.createdDate(toInstant(fields.path("created").asText(null)));
        ticketBuilder.issueType(fields.path("issuetype").path("name").asText());
        ticketBuilder.status(fields.path("status").path("name").asText());

        ticketBuilder.statusChangeDate(toInstant(fields.path("statuscategorychangedate").asText()));

        ticketBuilder.resolvedDate(toInstant(fields.path("resolutiondate").asText(null)));
        ticketBuilder.priority(fields.path("priority").path("name").asText("unspecified"));
        ticketBuilder.reporter(fields.path("reporter").path("emailAddress").asText(null));
        ticketBuilder.assignee(fields.path("assignee").path("emailAddress").asText(null));

        var labelNode = fields.path("labels");
        if (labelNode.isArray()) {
            var labels = StreamSupport.stream(labelNode.spliterator(), false).map(JsonNode::asText)
                    .collect(Collectors.toList());
            ticketBuilder.labels(labels);
        }

        ticketBuilder.team(fields.path("customfield_10800").path("name").asText());

        ticketBuilder.epic(fields.path("parent").path("fields").path("summary").asText(null));

        String epicKey = fields.path("parent").path("key").asText();
        if (epicKey != null && !epicKey.isBlank() && epics.containsKey(epicKey)) {
            ticketBuilder.initiative(epics.get(epicKey).initiative().orElse("Other"));
        }

        return ticketBuilder.build();
    }

    private Map<String, Epic> fetchEpics(JiraWebClientFactory jiraWebClientFactory) {

        Map<String, String> initatives = fetchInitiatives(jiraWebClientFactory);

        List<String> fields = new ArrayList<>() {
            {
                add("key");
                add("summary");
                add("parent");
            }
        };

        String query = "project in (ENG, T3) and issuetype = Epic ORDER BY created DESC";

        Map<String, Epic> epics = new HashMap<>();
        do {
            JsonNode response;
            try {
                response = jiraWebClientFactory.getWebClient().get()
                        .uri(uriBuilder -> uriBuilder.path("/rest/api/3/search").queryParam("jql", query)
                                .queryParam("startAt", epics.size()).queryParam("maxResults", 100)
                                .queryParam("fields", fields).build())
                        .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
            } catch (WebClientResponseException exception) {
                throw new RuntimeException("Unable to fetch engineering epics", exception);
            }
            if (response == null || !response.has("issues")) {
                throw new IllegalArgumentException(
                        "Jira search failed with unexpected response while fetching initatives");
            }

            var issues = response.path("issues");
            if (!issues.isArray()) {
                throw new IllegalArgumentException("Jira search failed with unexpected response");
            }

            for (JsonNode issue : issues) {
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
        } while (true);

        return epics;
    }

    private Map<String, String> fetchInitiatives(JiraWebClientFactory jiraWebClientFactory) {
        List<String> fields = new ArrayList<>() {
            {
                add("key");
                add("summary");
            }
        };

        String query = "project in (ENG, T3) and issuetype = Initiative ORDER BY created DESC";

        JsonNode response;
        try {
            response = jiraWebClientFactory.getWebClient().get()
                    .uri(uriBuilder -> uriBuilder.path("/rest/api/3/search").queryParam("jql", query)
                            .queryParam("fields", fields).build())
                    .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(JsonNode.class).block();
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
        for (JsonNode issue : issues) {
            JsonNode keyNode = issue.path("key");
            JsonNode summaryNode = issue.path("fields").path("summary");
            initiatives.put(keyNode.asText(), summaryNode.asText());
        }
        return initiatives;
    }

    public static record Epic(String key, String epic, Optional<String> initiative) {
    };

    private Instant toInstant(String date) {
        if (date == null) {
            return null;
        }

        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, ZONED_DATE_TIME_FORMATTER);
        ZonedDateTime utcTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

        return utcTime.toInstant();
    }
}
