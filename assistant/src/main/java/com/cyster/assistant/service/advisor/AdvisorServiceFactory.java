package com.cyster.assistant.service.advisor;

public interface AdvisorServiceFactory {
    AdvisorService createAdvisorService(String openAiApiKey);
}
