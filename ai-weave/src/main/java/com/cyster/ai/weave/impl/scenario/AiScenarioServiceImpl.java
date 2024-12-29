package com.cyster.ai.weave.impl.scenario;

import com.cyster.ai.weave.service.AiScenarioService;
import com.cyster.ai.weave.service.advisor.AiAdvisorService;
import com.cyster.ai.weave.service.scenario.ScenarioBuilder;

public class AiScenarioServiceImpl implements AiScenarioService {
    private final AiAdvisorService advisorService;

    public AiScenarioServiceImpl(AiAdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @Override
    public <PARAMETERS, CONTEXT> ScenarioBuilder<PARAMETERS, CONTEXT> getOrCreateScenario(String name) {
        return new ScenarioBuilderImpl<PARAMETERS, CONTEXT>(advisorService, name);
    }

}
