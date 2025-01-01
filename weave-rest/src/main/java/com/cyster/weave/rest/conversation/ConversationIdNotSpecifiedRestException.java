package com.cyster.weave.rest.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ConversationIdNotSpecifiedRestException extends RestException {
    public ConversationIdNotSpecifiedRestException() {
        super(HttpStatus.BAD_REQUEST, "No converersatoin id specified");
    }
}
