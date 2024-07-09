package com.cyster.weave.rest.scenario;


import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ScenarioNotFoundException extends RestException {
    public ScenarioNotFoundException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public ScenarioNotFoundException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}
