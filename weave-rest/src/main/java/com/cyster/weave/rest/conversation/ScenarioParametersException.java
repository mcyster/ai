package com.cyster.weave.rest.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ScenarioParametersException  extends RestException {

    public ScenarioParametersException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public ScenarioParametersException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }

}
