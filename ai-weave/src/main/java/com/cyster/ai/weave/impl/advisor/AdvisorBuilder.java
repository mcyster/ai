package com.cyster.ai.weave.impl.advisor;

import java.nio.file.Path;

import com.cyster.ai.weave.service.Tool;

public interface AdvisorBuilder<C> {

    AdvisorBuilder<C> setInstructions(String instruction);

    <T> AdvisorBuilder<C> withTool(Tool<T, C> tool);

    AdvisorBuilder<C> withFile(Path path);

    Advisor<C> getOrCreate();
}
