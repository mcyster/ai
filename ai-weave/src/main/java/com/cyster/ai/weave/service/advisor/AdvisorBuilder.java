package com.cyster.ai.weave.service.advisor;

import java.nio.file.Path;

import com.cyster.ai.weave.service.tool.Tool;

public interface AdvisorBuilder<CONTEXT> {

    AdvisorBuilder<CONTEXT> setInstructions(String instruction);

    <TOOL_PARAMETERS, TOOL_CONTEXT> AdvisorBuilder<CONTEXT> withTool(Tool<TOOL_PARAMETERS, TOOL_CONTEXT> tool);

    AdvisorBuilder<CONTEXT> withFile(Path path);

    Advisor<CONTEXT> getOrCreate();
}
