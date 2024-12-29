package com.cyster.ai.weave.service.scenario;

import com.cyster.ai.weave.service.tool.Tool;

public interface ScenarioBuilder<PARAMETERS, CONTEXT> {

    ScenarioBuilder<PARAMETERS, CONTEXT> setInstructions(String instruction);

    ScenarioBuilder<PARAMETERS, CONTEXT> withTool(Tool<?, ?> tool);

    Scenario<PARAMETERS, CONTEXT> getOrCreate();
}
