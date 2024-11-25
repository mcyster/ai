package com.cyster.jira.client.ticket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Ticket(String key, String project, String description, List<Comment> comments,
        Map<String, Object> fields) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        String key;
        String project;
        String description;
        List<Comment> comments = new ArrayList<>();
        Map<String, Object> fields = new HashMap<>();

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder project(String project) {
            this.project = project;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder addComment(String description, String author, String created) {
            comments.add(new Comment(description, author, created));
            return this;
        }

        public Builder addField(String name, Object value) {
            fields.put(name, value);
            return this;
        }

        public Ticket build() {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Ticket key cannot be null or blank");
            }

            if (description == null) {
                throw new IllegalArgumentException("Ticket description cannot be null");
            }

            return new Ticket(key, project, description, comments, fields);
        }
    }

    public record Comment(String description, String author, String createdDate) {
    };
}
