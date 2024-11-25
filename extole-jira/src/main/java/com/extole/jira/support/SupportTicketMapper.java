package com.extole.jira.support;

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
import com.cyster.jira.client.ticket.Ticket;
import com.cyster.jira.client.ticket.TicketMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

class SupportTicketMapper implements TicketMapper {
    private static final DateTimeFormatter ZONED_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_OUTPUT_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

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
            add("customfield_11100"); // organization
            add("customfield_11312"); // clientShortName-clientId
            add("customfield_11326"); // pod
            add("customfield_11375"); // pairCsm
            add("customfield_11373"); // clientPriority
            add("customfield_11320"); // clientId
            add("customfield_11390"); // requestedDueDate
            add("customfield_11391"); // estimatedDeliveryDate, format sad ...
            add("customfield_11379"); // RunbookUsage

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

    public Ticket issueToTicket(JsonNode issue) {
        var ticketBuilder = Ticket.newBuilder();

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

                    String createdDate = toUtcString(commentNode.path("created").asText(null));

                    ticketBuilder.addComment(description, author, createdDate);
                }
            }
        }

        JsonNode fields = issue.path("fields");

        ticketBuilder.addField("summary", fields.path("summary").asText());
        ticketBuilder.addField("project", fields.path("project").path("name").asText());
        ticketBuilder.addField("createdDate", toUtcString(fields.path("created").asText(null)));
        ticketBuilder.addField("issueType", fields.path("issuetype").path("name").asText());
        ticketBuilder.addField("status", fields.path("status").path("name").asText());

        ticketBuilder.addField("statusChangedDate", toUtcString(fields.path("statuscategorychangedate").asText(null)));
        ticketBuilder.addField("category", fields.path("parent").path("fields").path("summary").asText(null));
        ticketBuilder.addField("resolvedDate", toUtcString(fields.path("resolutiondate").asText(null)));

        var labelNode = fields.path("labels");
        if (labelNode.isArray()) {
            var labels = StreamSupport.stream(labelNode.spliterator(), false).map(JsonNode::asText)
                    .collect(Collectors.toList());
            ticketBuilder.addField("labels", labels);
        }

        ticketBuilder.addField("priority", fields.path("priority").path("name").asText());
        ticketBuilder.addField("reporter", fields.path("reporter").path("emailAddress").asText(null));
        ticketBuilder.addField("assignee", fields.path("assignee").path("emailAddress").asText(null));

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
        ticketBuilder.addField("clientShortName", clientShortName);

        ticketBuilder.addField("clientId", fields.path("customfield_11320").asText(null));

        String pod = fields.path("customfield_11326").asText(null);
        if (pod != null) {
            pod = pod.replaceAll("(?i)^support\\s*", "").replaceAll("(?i)\\s*pod$", "");
        }
        ticketBuilder.addField("pod", pod);

        ticketBuilder.addField("pairCsm", fields.path("customfield_11375").path("emailAddress").asText(null));
        ticketBuilder.addField("clientPriority", fields.path("customfield_11373").asText(null));
        ticketBuilder.addField("timeSeconds", fields.path("aggregatetimespent").asInt());

        String requestedDueDate = toUtcString(fields.path("customfield_11390").asText(null));
        ticketBuilder.addField("requestedDueDate", requestedDueDate);

        if (requestedDueDate != null) {
            ZonedDateTime dueDate = ZonedDateTime.parse(requestedDueDate, DateTimeFormatter.ISO_ZONED_DATE_TIME);

            ZonedDateTime startDate = dueDate.minus(8, ChronoUnit.DAYS);
            ticketBuilder.addField("requestedStartDate", startDate.format(DATE_OUTPUT_FORMATTER));

            ZonedDateTime createdDate = ZonedDateTime.parse(toUtcString(fields.path("created").asText(null)),
                    DateTimeFormatter.ISO_ZONED_DATE_TIME);

            if (startDate.isAfter(createdDate)) {
                ticketBuilder.addField("startDate", startDate.format(DATE_OUTPUT_FORMATTER));
            } else {
                ticketBuilder.addField("startDate", createdDate.format(DATE_OUTPUT_FORMATTER));
            }

        } else {
            ticketBuilder.addField("requestedStartDate", null);
            ticketBuilder.addField("startDate", ZonedDateTime.parse(toUtcString(fields.path("created").asText(null))));
        }

        return ticketBuilder.build();
    }

    private String toUtcString(String date) {
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

        return zonedDateTime.format(DATE_OUTPUT_FORMATTER);
    }
}
