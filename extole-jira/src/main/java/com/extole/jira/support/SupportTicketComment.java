package com.extole.jira.support;

import java.time.ZonedDateTime;

public record SupportTicketComment(String description, String author, ZonedDateTime created) {
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        String description = "";
        String author;
        ZonedDateTime created;
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }
        
        public Builder created(String created) {
            this.created = SupportTicket.Builder.parseZonedDateTime(created);
            return this;
        }
        
        public SupportTicketComment build() {
            if (author == null || author.isEmpty()) {
                throw new IllegalArgumentException("Author cannot be null or empty");
            }
            if (created == null) {
                throw new IllegalArgumentException("Created cannot be null");
            }
            
            return new SupportTicketComment(description, author, created);
        }
    }
};

