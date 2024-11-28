package com.extole.jira.engineering;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record EngineeringTicket(String key, String project, String summary, String description, Instant createdDate,
        Instant resolvedDate, String priority, String issueType, String status, List<String> labels, String reporter,
        String assignee, String epic, String initiative, Instant statusChangeDate, String team,
        List<Comment> comments) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String key;
        private String project;
        private String summary;
        private String description;
        private Instant createdDate;
        private Instant resolvedDate;
        private String priority;
        private String issueType;
        private String status;
        private List<String> labels = new ArrayList<>();
        private String reporter;
        private String assignee;
        private String epic;
        private String initiative;
        private Instant statusChangeDate;
        private String team;
        private List<Comment> comments = new ArrayList<>();

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder project(String project) {
            this.project = project;
            return this;
        }

        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder createdDate(Instant createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public Builder resolvedDate(Instant resolvedDate) {
            this.resolvedDate = resolvedDate;
            return this;
        }

        public Builder priority(String priority) {
            this.priority = priority;
            return this;
        }

        public Builder issueType(String issueType) {
            this.issueType = issueType;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder labels(List<String> labels) {
            this.labels = labels;
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

        public Builder epic(String epic) {
            this.epic = epic;
            return this;
        }

        public Builder initiative(String initiative) {
            this.initiative = initiative;
            return this;
        }

        public Builder statusChangeDate(Instant statusChangeDate) {
            this.statusChangeDate = statusChangeDate;
            return this;
        }

        public Builder team(String team) {
            this.team = team;
            return this;
        }

        public Builder comments(List<Comment> comments) {
            this.comments = comments;
            return this;
        }

        public Builder addComment(String description, String author, Instant created) {
            comments.add(new Comment(description, author, created));
            return this;
        }

        public EngineeringTicket build() {
            return new EngineeringTicket(key, project, summary, description, createdDate, resolvedDate, priority,
                    issueType, status, labels, reporter, assignee, epic, initiative, statusChangeDate, team, comments);
        }
    }

    public record Comment(String description, String author, Instant createdDate) {
    }
}
