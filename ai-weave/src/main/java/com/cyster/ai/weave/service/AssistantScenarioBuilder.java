package com.cyster.ai.weave.service;

import com.cyster.ai.weave.service.scenario.Scenario;

public interface AssistantScenarioBuilder<PARAMETERS, CONTEXT> {

    AssistantScenarioBuilder<PARAMETERS, CONTEXT> setInstructions(String instruction);

    AssistantScenarioBuilder<PARAMETERS, CONTEXT> withTool(Tool<?, ?> tool);

    Scenario<PARAMETERS, CONTEXT> getOrCreate();
}
