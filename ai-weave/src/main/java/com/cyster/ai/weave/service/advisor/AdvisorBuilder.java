package com.cyster.ai.weave.service.advisor;

import java.nio.file.Path;

import com.cyster.ai.weave.service.tool.Tool;

public interface AdvisorBuilder<CONTEXT> {

    AdvisorBuilder<CONTEXT> setInstructions(String instruction);

    AdvisorBuilder<CONTEXT> withTool(Tool<?, CONTEXT> tool);

    AdvisorBuilder<CONTEXT> withFile(Path path);

    Advisor<CONTEXT> getOrCreate();
}
