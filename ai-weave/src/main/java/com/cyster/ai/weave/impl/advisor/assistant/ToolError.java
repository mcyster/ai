package com.cyster.ai.weave.impl.advisor.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class ToolError {
    private final String error;

    enum Type {
        RETRYABLE,
        BAD_TOOL_NAME,
        BAD_TOOL_PARAMETERS,
        FATAL_TOOL_ERROR
    }

    private final Type errorType;

    ToolError(String error, Type errorType) {
        this.error = error;
        this.errorType = errorType;
    }

    @JsonProperty("error")
    public String getError() {
        return this.error;
    }

    @JsonProperty("error_type")
    public Type getErrorTtype() {
        return this.errorType;
    }

    String toJsonString() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Tool bad call parameters", e);
        }
    }
}
