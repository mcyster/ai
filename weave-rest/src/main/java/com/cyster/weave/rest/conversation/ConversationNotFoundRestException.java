package com.cyster.weave.rest.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ConversationNotFoundRestException extends RestException {
    private String id;

    public ConversationNotFoundRestException(String sessionId) {
        super(HttpStatus.BAD_REQUEST, getMessage(sessionId));
        this.id = sessionId;
    }

    ConversationNotFoundRestException(String id, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, getMessage(id), cause);
        this.id = id;
    }

    public String getSessionId() {
        return this.id;
    }

    private static String getMessage(String id) {
        return "Conversatoin id '" + id + "' not found";
    }
}
