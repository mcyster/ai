package com.cyster.ai.weave.service;

import com.cyster.ai.weave.service.advisor.AdvisorBuilder;

public interface AiAdvisorService {
    <CONTEXT> AdvisorBuilder<CONTEXT> getOrCreateAdvisorBuilder(String name);
}
