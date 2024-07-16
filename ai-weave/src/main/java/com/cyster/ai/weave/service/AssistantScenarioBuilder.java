package com.cyster.ai.weave.service;

import com.cyster.ai.weave.service.scenario.Scenario;

public interface AssistantScenarioBuilder<PARAMETERS, CONTEXT> {
    
    AssistantScenarioBuilder<PARAMETERS, CONTEXT> setInstructions(String instruction);

    <TOOL_PARAMETERS> AssistantScenarioBuilder<PARAMETERS, CONTEXT> withTool(Tool<TOOL_PARAMETERS, CONTEXT> tool);

    Scenario<PARAMETERS, CONTEXT> getOrCreate();
}
