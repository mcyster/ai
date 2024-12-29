package com.cyster.ai.weave.service;

import com.cyster.ai.weave.service.scenario.ScenarioBuilder;

public interface AiScenarioService {

    <PARAMETERS, CONTEXT> ScenarioBuilder<PARAMETERS, CONTEXT> getOrCreateScenario(String name);

}
