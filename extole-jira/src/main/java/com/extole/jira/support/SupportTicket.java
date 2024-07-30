package com.extole.jira.support;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record SupportTicket(
    String key,
    String project,
    String type,
    String status,
    ZonedDateTime created,
    ZonedDateTime statusChanged,
    Optional<String> category,
    Optional<ZonedDateTime> resolved,
    Optional<ZonedDateTime> due,
    String priority,
    Optional<String> clientPriority,
    Optional<String> reporter,
    Optional<String> assignee,
    Optional<String> client,
    Optional<String> clientId,
    Optional<String> pod,
    Optional<String> pairCsm,
    Optional<String> pairSupport,
    Integer timeSeconds,
    String summary,
    List<String> labels
) {
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private static final DateTimeFormatter ZONED_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String key = null;
        String project = null;
        String type = null;
        String status = null;
        ZonedDateTime created = null;
        ZonedDateTime statusChanged = null;
        Optional<String> category = Optional.empty();
        Optional<ZonedDateTime> resolved = Optional.empty();
        Optional<ZonedDateTime> due;
        String priority;
        Optional<String> clientPriority = Optional.empty();
        Optional<String> reporter = Optional.empty();
        Optional<String> assignee = Optional.empty();
        Optional<String> client = Optional.empty();
        Optional<String> clientId = Optional.empty();
        Optional<String> pod = Optional.empty();
        Optional<String> pairCsm = Optional.empty();
        Optional<String> pairSupport = Optional.empty();
        Integer timeSeconds = 0;
        String summary = null;
        List<String> labels = Collections.emptyList(); 
                
        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder project(String project) {
            this.project = project;
            return this;
        }

        public Builder created(String created) {
            this.created = parseZonedDateTime(created);
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
            this.statusChanged = parseZonedDateTime(statusChanged);
            return this;
        }

        public Builder category(String category) {
            this.category = Optional.ofNullable(category);
            return this;
        }

        public Builder resolved(String resolved) {
            if (resolved != null && !resolved.isBlank()) {
                this.resolved = Optional.ofNullable(parseZonedDateTime(resolved));
            } 
            return this;
        }

        public Builder due(String due) {
            this.due = Optional.ofNullable(parseDate(due));
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

        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public Builder labels(List<String> labels) {
            this.labels = labels;
            return this;
        }
        
        public SupportTicket build() {
            if (key == null || key.isEmpty()) {
                throw new IllegalArgumentException("Key cannot be null or empty");
            }
            if (project == null || project.isEmpty()) {
                throw new IllegalArgumentException("Project cannot be null or empty");
            }
            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException("Type cannot be null or empty");
            }
            if (status == null || status.isEmpty()) {
                throw new IllegalArgumentException("Status cannot be null or empty");
            }
            if (created == null) {
                throw new IllegalArgumentException("Created date cannot be null");
            }
            if (statusChanged == null) {
                throw new IllegalArgumentException("Status changed date cannot be null");
            }
            if (priority == null || priority.isEmpty()) {
                throw new IllegalArgumentException("Priority cannot be null or empty");
            }

            if (summary == null || summary.isEmpty()) {
                throw new IllegalArgumentException("Summary cannot be null or empty");
            }
            
            return new SupportTicket(
                    key,
                    project,
                    type,
                    status,
                    created,
                    statusChanged,
                    category,
                    resolved,
                    due,
                    priority,
                    clientPriority,
                    reporter,
                    assignee,
                    client,
                    clientId,
                    pod,
                    pairCsm,
                    pairSupport,
                    timeSeconds,
                    summary,
                    labels
                );
        }
        
        static ZonedDateTime parseZonedDateTime(String dateTime) {
            return ZonedDateTime.parse(dateTime, ZONED_DATE_TIME_FORMATTER);
        }
        
        static ZonedDateTime parseDate(String date) {
            if (date == null || date.isBlank()) {
                return null;
            }
            
            LocalDate localDate = LocalDate.parse(date, DATE_FORMATTER);

            return localDate.atStartOfDay(ZoneId.of("UTC"));
        }
    }
}
    
