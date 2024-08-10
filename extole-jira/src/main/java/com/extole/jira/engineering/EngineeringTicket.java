package com.extole.jira.engineering;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record EngineeringTicket (
    String key,
    String project,
    String type,
    String status,
    ZonedDateTime statusChanged,
    Optional<String> epic,
    Optional<String> initiative,
    ZonedDateTime created,
    Optional<ZonedDateTime> resolved,
    String priority,
    Optional<String> reporter,
    Optional<String> assignee,
    Optional<String> team,
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
        ZonedDateTime statusChanged = null;
        Optional<String> epic = Optional.empty();
        Optional<String> initiative = Optional.empty();
        ZonedDateTime created = null;
        Optional<ZonedDateTime> resolved = Optional.empty();
        String priority;
        Optional<String> reporter = Optional.empty();
        Optional<String> assignee = Optional.empty();
        Optional<String> team = Optional.empty();
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

        public Builder epic(String epic) {
            this.epic = Optional.ofNullable(epic);
            return this;
        }

        public Builder initiative(String initiative) {
            this.epic = Optional.ofNullable(initiative);
            return this;
        }

        public Builder created(String created) {
            this.created = parseZonedDateTime(created);
            return this;
        }

        public Builder resolved(String resolved) {
            if (resolved != null && !resolved.isBlank()) {
                this.resolved = Optional.ofNullable(parseZonedDateTime(resolved));
            } 
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

        public Builder team(String team) {
            this.team = Optional.ofNullable(team);
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
        
        public EngineeringTicket build() {
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
            
            return new EngineeringTicket(
                    key,
                    project,
                    type,
                    status,
                    statusChanged,
                    epic,
                    initiative,
                    created,
                    resolved,
                    priority,
                    reporter,
                    assignee,
                    team,
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

