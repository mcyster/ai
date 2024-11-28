package com.cyster.jira.client.ticket.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.cyster.jira.client.adf.reader.MarkDownDocumentMapper;
import com.cyster.jira.client.ticket.TicketMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class DefaultTicketMapper implements TicketMapper<Ticket> {
    private static final DateTimeFormatter ZONED_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

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
            add("description");
            add("comment");
        }
    };

    private final List<String> projects = new ArrayList<>();

    public List<String> projects() {
        return projects;
    }

    public List<String> fields() {
        return fields;
    }

    public Ticket issueToTicket(JsonNode issue) {
        var ticketBuilder = Ticket.builder();

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
        ticketBuilder.resolvedDate(toInstant(fields.path("resolutiondate").asText(null)));
        ticketBuilder.issueType(fields.path("issuetype").path("name").asText());
        ticketBuilder.status(fields.path("status").path("name").asText());
        ticketBuilder.priority(fields.path("priority").path("name").asText("unspecified"));
        ticketBuilder.reporter(fields.path("reporter").path("emailAddress").asText(null));
        ticketBuilder.assignee(fields.path("assignee").path("emailAddress").asText(null));

        var labelNode = fields.path("labels");
        if (labelNode.isArray()) {
            var labels = StreamSupport.stream(labelNode.spliterator(), false).map(JsonNode::asText)
                    .collect(Collectors.toList());
            ticketBuilder.labels(labels);
        }

        return ticketBuilder.build();
    }

    private Instant toInstant(String date) {
        if (date == null) {
            return null;
        }

        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, ZONED_DATE_TIME_FORMATTER);
        ZonedDateTime utcTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

        return utcTime.toInstant();
    }

}
