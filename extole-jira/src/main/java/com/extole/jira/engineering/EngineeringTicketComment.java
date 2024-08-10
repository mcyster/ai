package com.extole.jira.engineering;

import java.time.ZonedDateTime;

public record EngineeringTicketComment(String description, String author, ZonedDateTime created) {
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
            this.created = EngineeringTicket.Builder.parseZonedDateTime(created);
            return this;
        }
        
        public EngineeringTicketComment build() {
            if (author == null || author.isEmpty()) {
                throw new IllegalArgumentException("Author cannot be null or empty");
            }
            if (created == null) {
                throw new IllegalArgumentException("Created cannot be null");
            }
            
            return new EngineeringTicketComment(description, author, created);
        }
    }
};


