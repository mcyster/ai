package com.cyster.ai.weave.service.advisor;

import java.nio.file.Path;

import com.cyster.ai.weave.service.tool.CodeInterpreterTool;
import com.cyster.ai.weave.service.tool.SearchTool;
import com.cyster.ai.weave.service.tool.Tool;

public interface AdvisorBuilder<CONTEXT> {

    AdvisorBuilder<CONTEXT> setInstructions(String instruction);

    AdvisorBuilder<CONTEXT> withTool(Tool<?, CONTEXT> tool);

    SearchTool.Builder<CONTEXT> searchToolBuilder(Class<CONTEXT> contextClass);

    CodeInterpreterTool.Builder<CONTEXT> codeToolBuilder(Class<CONTEXT> contextClass);

    AdvisorBuilder<CONTEXT> withFile(Path path);

    Advisor<CONTEXT> getOrCreate();
}
