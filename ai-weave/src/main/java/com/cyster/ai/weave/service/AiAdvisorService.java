package com.cyster.ai.weave.service;

import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.tool.CodeInterpreterTool;
import com.cyster.ai.weave.service.tool.SearchTool;

public interface AiAdvisorService {
    <CONTEXT> AdvisorBuilder<CONTEXT> getOrCreateAdvisorBuilder(String name);

    SearchTool.Builder searchToolBuilder();

    CodeInterpreterTool.Builder codeToolBuilder();
}
