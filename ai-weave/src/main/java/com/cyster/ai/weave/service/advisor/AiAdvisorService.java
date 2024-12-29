package com.cyster.ai.weave.service.advisor;

import com.cyster.ai.weave.service.DocumentStore.DirectoryDocumentStoreBuilder;
import com.cyster.ai.weave.service.DocumentStore.SimpleDocumentStoreBuilder;
import com.cyster.ai.weave.service.tool.CodeInterpreterTool;
import com.cyster.ai.weave.service.tool.SearchTool;
import com.cyster.ai.weave.service.tool.Tool;

public interface AiAdvisorService {
    <CONTEXT> AdvisorBuilder<CONTEXT> getOrCreateAdvisorBuilder(String name);

    <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool);

    SearchTool.Builder searchToolBuilder();

    CodeInterpreterTool.Builder codeToolBuilder();

    SimpleDocumentStoreBuilder simpleDocumentStoreBuilder();

    DirectoryDocumentStoreBuilder directoryDocumentStoreBuilder();
}
