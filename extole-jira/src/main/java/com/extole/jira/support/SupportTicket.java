package com.extole.jira.support;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record SupportTicket(String key, String project, String summary, String description, Instant createdDate,
        Instant resolvedDate, String priority, String issueType, String status, List<String> labels, String reporter,
        String assignee, String runbook, String runbookUsage, String activity, String activityCategory,
        String clientShortName, String clientId, Instant statusChangeDate, String csm, String pod, String workType,
        String clientPriority, Duration duration, Instant startDate, Instant requestedStartDate,
        Instant requestedDueDate, List<Comment> comments) {

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
        private String runbook;
        private String runbookUsage;
        private String activity;
        private String activityCategory;
        private String clientShortName;
        private String clientId;
        private Instant statusChangeDate;
        private String csm;
        private String pod;
        private String workType;
        private String clientPriority;
        private Duration duration;
        private Instant startDate;
        private Instant requestedStartDate;
        private Instant requestedDueDate;
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

        public Builder runbook(String runbook) {
            this.runbook = runbook;
            return this;
        }

        public Builder runbookUsage(String runbookUsage) {
            this.runbookUsage = runbookUsage;
            return this;
        }

        public Builder activity(String activity) {
            this.activity = activity;
            return this;
        }

        public Builder activityCategory(String activityCategory) {
            this.activityCategory = activityCategory;
            return this;
        }

        public Builder clientShortName(String clientShortName) {
            this.clientShortName = clientShortName;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder statusChangeDate(Instant statusChangeDate) {
            this.statusChangeDate = statusChangeDate;
            return this;
        }

        public Builder csm(String csm) {
            this.csm = csm;
            return this;
        }

        public Builder pod(String pod) {
            this.pod = pod;
            return this;
        }

        public Builder workType(String workType) {
            this.workType = workType;
            return this;
        }

        public Builder clientPriority(String clientPriority) {
            this.clientPriority = clientPriority;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder startDate(Instant startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder requestedStartDate(Instant requestedStartDate) {
            this.requestedStartDate = requestedStartDate;
            return this;
        }

        public Builder requestedDueDate(Instant requestedDueDate) {
            this.requestedDueDate = requestedDueDate;
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

        public SupportTicket build() {
            return new SupportTicket(key, project, summary, description, createdDate, resolvedDate, priority, issueType,
                    status, labels, reporter, assignee, runbook, runbookUsage, activity, activityCategory,
                    clientShortName, clientId, statusChangeDate, csm, pod, workType, clientPriority, duration,
                    startDate, requestedStartDate, requestedDueDate, comments);
        }
    }

    public record Comment(String description, String author, Instant createdDate) {
    }
}
