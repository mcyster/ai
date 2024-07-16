package com.cyster.ai.weave.impl.advisor;

public interface AdvisorServiceFactory {
    AdvisorService createAdvisorService(String openAiApiKey);
}
