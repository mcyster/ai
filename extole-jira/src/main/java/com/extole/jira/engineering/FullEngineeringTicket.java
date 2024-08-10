package com.extole.jira.engineering;

import java.util.ArrayList;
import java.util.List;

public record FullEngineeringTicket(EngineeringTicket ticket, String description, List<EngineeringTicketComment> comments) {

    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        EngineeringTicket ticket;
        String description;
        List<EngineeringTicketComment> comments = new ArrayList<>();
    
        public Builder ticket(EngineeringTicket ticket) {
            this.ticket = ticket;
            return this;
        }
    
        public Builder description(String description) {
            this.description = description;
            return this;
        }
          
        public Builder addComment(EngineeringTicketComment comment) {
            comments.add(comment);
            return this;
        }

        public FullEngineeringTicket build() {
            if (ticket == null) {
                throw new IllegalArgumentException("Ticket cannot be null");
            }
        
            if (description == null) {
                throw new IllegalArgumentException("Description cannot be null");
            }
         
            
            return new FullEngineeringTicket(ticket, description, comments);
        }
    }
}

