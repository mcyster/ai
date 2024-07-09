package com.cyster.weave.rest.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ScenarioSessionNotSpecifiedRestException extends RestException {
    public ScenarioSessionNotSpecifiedRestException() {
        super(HttpStatus.BAD_REQUEST, "No scenario session_id specified");
    }
}
