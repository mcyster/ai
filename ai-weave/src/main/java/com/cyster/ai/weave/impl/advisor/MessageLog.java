package com.cyster.ai.weave.impl.advisor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public record MessageLog(
    List<LogEntry> log) {
    
    public MessageLog {
        log = Collections.unmodifiableList(new ArrayList<>(log));
    }
    
    void addLog(String description, Object details) {
        log.add(new LogEntry(description, details));
    }
    
    public static class Builder {
        private final List<LogEntry> logEntries = new ArrayList<>();

        public Builder addLog(String description, Object details) {
            logEntries.add(new LogEntry(description, details));
            return this;
        }

        public MessageLog build() {
            return new MessageLog(logEntries);
        }
    }
    
    static record LogEntry(String description, Object details) {
        @Override
        public String toString() {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
