package com.cyster.weave.rest.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ConversationRestException extends RestException {
    private String conversationId;

    public ConversationRestException(String conversationId) {
        super(HttpStatus.BAD_REQUEST, getMessage(conversationId));
        this.conversationId = conversationId;
    }

    ConversationRestException(String name, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, getMessage(name), cause);
    }

    public String getConversationId() {
        return this.conversationId;
    }

    private static String getMessage(String conversationId) {
        return "Conversation error for conversationId: " + conversationId;
    }
}
