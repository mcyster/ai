package com.cyster.ai.weave.impl.openai.advisor.assistant;

public class AssistantAdvisorConversationException extends Exception {
    public AssistantAdvisorConversationException(String message) {
        super(message);
    }

    public AssistantAdvisorConversationException(String message, Throwable cause) {
        super(message, cause);
    }
}
