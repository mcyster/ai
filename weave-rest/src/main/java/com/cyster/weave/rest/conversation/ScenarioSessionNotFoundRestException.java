package com.cyster.weave.rest.conversation;

import org.springframework.http.HttpStatus;

import com.cyster.rest.RestException;

public class ScenarioSessionNotFoundRestException extends RestException {
    private String sessionId;

    public ScenarioSessionNotFoundRestException(String sessionId) {
        super(HttpStatus.BAD_REQUEST, getMessage(sessionId));
        this.sessionId = sessionId;
    }

    ScenarioSessionNotFoundRestException(String sessionId, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, getMessage(sessionId), cause);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    private static String getMessage(String session_id) {
        return "Scenario session_id '" + session_id + "' not found";
    }
}
