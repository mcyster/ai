package com.cyster.ai.weave.impl.advisor.assistant;

public class AdvisorConversationException extends Exception {
    public AdvisorConversationException(String message) {
        super(message);
    }

    public AdvisorConversationException(String message, Throwable cause) {
        super(message, cause);
    }
}
