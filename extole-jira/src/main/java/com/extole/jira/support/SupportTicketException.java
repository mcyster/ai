package com.extole.jira.support;

public class SupportTicketException extends Exception {
    public SupportTicketException(String message) {
        super(message);
    }

    public SupportTicketException(String message, Throwable cause) {
        super(message, cause);
    }    

}
