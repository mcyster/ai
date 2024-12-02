package com.cyster.ai.weave.impl.advisor;

import java.nio.file.Path;

import com.cyster.ai.weave.service.Tool;

public interface AdvisorBuilder<SCENARIO_CONTEXT> {

    AdvisorBuilder<SCENARIO_CONTEXT> setInstructions(String instruction);

    <TOOL_PARAMETERS, TOOL_CONTEXT> AdvisorBuilder<SCENARIO_CONTEXT> withTool(Tool<TOOL_PARAMETERS, TOOL_CONTEXT> tool);

    AdvisorBuilder<SCENARIO_CONTEXT> withFile(Path path);

    Advisor<SCENARIO_CONTEXT> getOrCreate();
}
