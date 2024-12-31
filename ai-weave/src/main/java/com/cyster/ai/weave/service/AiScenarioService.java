package com.cyster.ai.weave.service;

import com.cyster.ai.weave.service.scenario.ScenarioBuilder;
import com.cyster.ai.weave.service.scenario.ScenarioSetBuilder;

public interface AiScenarioService {

    <PARAMETERS, CONTEXT> ScenarioBuilder<PARAMETERS, CONTEXT> getOrCreateScenarioBuilder(String name);

    <CONTEXT> ScenarioSetBuilder<CONTEXT> senarioSetBuilder();

}
