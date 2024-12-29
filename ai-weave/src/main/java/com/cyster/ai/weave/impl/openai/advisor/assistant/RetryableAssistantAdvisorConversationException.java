package com.cyster.ai.weave.impl.openai.advisor.assistant;

public class RetryableAssistantAdvisorConversationException extends AssistantAdvisorConversationException {
    public RetryableAssistantAdvisorConversationException(String message) {
        super(message);
    }

    public RetryableAssistantAdvisorConversationException(String message, Throwable cause) {
        super(message, cause);
    }
}
