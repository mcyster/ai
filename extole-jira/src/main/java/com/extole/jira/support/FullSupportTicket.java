package com.extole.jira.support;

import java.util.ArrayList;
import java.util.List;

public record FullSupportTicket(SupportTicket ticket, String description, List<TicketComment> comments) {

    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        SupportTicket ticket;
        String description;
        List<TicketComment> comments = new ArrayList<>();
    
        public Builder ticket(SupportTicket ticket) {
            this.ticket = ticket;
            return this;
        }
    
        public Builder description(String description) {
            this.description = description;
            return this;
        }
          
        public Builder addComment(TicketComment comment) {
            comments.add(comment);
            return this;
        }

        public FullSupportTicket build() {
            if (ticket == null) {
                throw new IllegalArgumentException("Ticket cannot be null");
            }
        
            if (description == null) {
                throw new IllegalArgumentException("Description cannot be null");
            }
         
            
            return new FullSupportTicket(ticket, description, comments);
        }
    }
}
