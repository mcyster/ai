package com.extole.jira.support;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.cyster.jira.client.adf.reader.MarkDownDocumentMapper;
import com.cyster.jira.client.ticket.TicketMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

class SupportTicketMapper implements TicketMapper<SupportTicket> {
    private static final DateTimeFormatter ZONED_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final List<String> fields = new ArrayList<>() {
        {
            add("key");
            add("project");
            add("created");
            add("summary");
            add("parent");
            add("issuetype");
            add("status");
            add("labels");
            add("statuscategorychangedate");
            add("reporter");
            add("assignee");
            add("resolutiondate");
            add("description");
            add("comment");
            add("customfield_11100"); // organization
            add("customfield_11312"); // clientShortName-clientId
            add("customfield_11326"); // pod
            add("customfield_11375"); // pairCsm
            add("customfield_11373"); // clientPriority
            add("customfield_11320"); // clientId
            add("customfield_11390"); // requestedDueDate
            add("customfield_11391"); // estimatedDeliveryDate, format sad ...
            add("customfield_11379"); // RunbookUsage
            add("customfield_11392"); // Activity
            add("aggregatetimespent");

        }
    };

    private final List<String> projects = List.of("HELP", "SUP", "LAUNCH", "SPEED");

    public List<String> projects() {
        return projects;
    }

    public List<String> fields() {
        return fields;
    }

    public SupportTicket issueToTicket(JsonNode issue) {
        var ticketBuilder = SupportTicket.builder();

        ticketBuilder.key(issue.path("key").asText());
        ticketBuilder.project(issue.path("fields").path("project").path("name").asText());

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

        ticketBuilder.summary(fields.path("summary").asText());
        ticketBuilder.project(fields.path("project").path("name").asText());

        Instant createdDate = toInstant(fields.path("created").asText(null));
        ticketBuilder.createdDate(createdDate);

        ticketBuilder.issueType(fields.path("issuetype").path("name").asText());
        ticketBuilder.status(fields.path("status").path("name").asText());

        ticketBuilder.statusChangeDate(toInstant(fields.path("statuscategorychangedate").asText(null)));

        ticketBuilder.runbook(fields.path("parent").path("fields").path("summary").asText(null));
        ticketBuilder.runbookUsage(fields.path("customfield_11379").asText(null));

        JsonNode activity = fields.path("customfield_11392");
        if (activity != null) {
            ticketBuilder.activityCategory(activity.path("value").asText("Uncategorized"));
            ticketBuilder.activity(activity.path("child").path("value").asText("Uncategorized"));
        } else {
            ticketBuilder.activityCategory("Uncategorized");
            ticketBuilder.activity("Uncategorized");
        }

        ticketBuilder.resolvedDate(toInstant(fields.path("resolutiondate").asText(null)));

        var labelNode = fields.path("labels");
        if (labelNode.isArray()) {
            var labels = StreamSupport.stream(labelNode.spliterator(), false).map(JsonNode::asText)
                    .collect(Collectors.toList());
            ticketBuilder.labels(labels);
        }

        ticketBuilder.priority(fields.path("priority").path("name").asText("unspecified"));
        ticketBuilder.reporter(fields.path("reporter").path("emailAddress").asText(null));
        ticketBuilder.assignee(fields.path("assignee").path("emailAddress").asText(null));

        String clientShortName = null;
        JsonNode organizationField = fields.path("customfield_11100");
        JsonNode organizationNode = organizationField.isArray() && organizationField.size() > 0
                ? organizationField.get(0)
                : null;
        if (organizationNode != null) {
            clientShortName = organizationNode.get("name").asText();
        }
        if (clientShortName == null) {
            String value = fields.path("customfield_11312").path("value").asText();
            if (value != null && !value.trim().isEmpty()) {
                clientShortName = value.split("-")[0].trim().replaceAll("(?i) VIP$", "");
                ;
            }
        }
        ticketBuilder.clientShortName(clientShortName);

        ticketBuilder.clientId(fields.path("customfield_11320").asText(null));

        String pod = fields.path("customfield_11326").asText(null);
        if (pod != null) {
            pod = pod.replaceAll("(?i)^support\\s*", "").replaceAll("(?i)\\s*pod$", "");
        }
        ticketBuilder.pod(pod);

        ticketBuilder.csm(fields.path("customfield_11375").path("emailAddress").asText(null));
        ticketBuilder.clientPriority(fields.path("customfield_11373").asText("Unspecified"));
        ticketBuilder.duration(Duration.ofSeconds(fields.path("aggregatetimespent").asInt()));

        Instant requestedDueDate = toInstant(fields.path("customfield_11390").asText(null));
        ticketBuilder.requestedDueDate(requestedDueDate);

        ticketBuilder.createdDate(createdDate);

        if (requestedDueDate != null) {
            Instant startDate = requestedDueDate.minus(8, ChronoUnit.DAYS);
            ticketBuilder.requestedStartDate(startDate);

            if (startDate.isAfter(createdDate)) {
                ticketBuilder.startDate(startDate);
            } else {
                ticketBuilder.startDate(createdDate);
            }

        } else {
            ticketBuilder.requestedStartDate(null);
            ticketBuilder.startDate(createdDate);
        }

        return ticketBuilder.build();
    }

    private Instant toInstant(String date) {
        if (date == null) {
            return null;
        }

        ZonedDateTime zonedDateTime;
        try {
            zonedDateTime = ZonedDateTime.parse(date, ZONED_DATE_TIME_FORMATTER).withZoneSameInstant(ZoneId.of("UTC"));
        } catch (Exception e) {
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);
            zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
        }

        return zonedDateTime.toInstant();
    }
}
