package com.cyster.weave.rest.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ScenarioContextException extends RestException {
    public ScenarioContextException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public ScenarioContextException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}
