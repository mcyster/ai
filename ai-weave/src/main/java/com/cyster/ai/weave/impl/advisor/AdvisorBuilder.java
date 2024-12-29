package com.cyster.ai.weave.impl.advisor;

import java.nio.file.Path;

import com.cyster.ai.weave.service.Tool;

public interface AdvisorBuilder<CONTEXT> {

    AdvisorBuilder<CONTEXT> setInstructions(String instruction);

    <TOOL_PARAMETERS, TOOL_CONTEXT> AdvisorBuilder<CONTEXT> withTool(Tool<TOOL_PARAMETERS, TOOL_CONTEXT> tool);

    AdvisorBuilder<CONTEXT> withFile(Path path);

    Advisor<CONTEXT> getOrCreate();
}
