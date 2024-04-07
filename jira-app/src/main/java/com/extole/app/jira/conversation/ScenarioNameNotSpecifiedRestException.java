package com.extole.app.jira.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ScenarioNameNotSpecifiedRestException extends RestException {
    public ScenarioNameNotSpecifiedRestException() {
        super(HttpStatus.BAD_REQUEST, "No scenario name specified");
    }

}
