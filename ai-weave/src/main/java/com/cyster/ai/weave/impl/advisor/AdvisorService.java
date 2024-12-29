package com.cyster.ai.weave.impl.advisor;

import com.cyster.ai.weave.service.CodeInterpreterTool;
import com.cyster.ai.weave.service.DocumentStore.DirectoryDocumentStoreBuilder;
import com.cyster.ai.weave.service.DocumentStore.SimpleDocumentStoreBuilder;
import com.cyster.ai.weave.service.SearchTool;
import com.cyster.ai.weave.service.Tool;

public interface AdvisorService {
    <CONTEXT> AdvisorBuilder<CONTEXT> getOrCreateAdvisor(String name);

    <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool);

    SearchTool.Builder searchToolBuilder();

    CodeInterpreterTool.Builder codeToolBuilder();

    SimpleDocumentStoreBuilder simpleDocumentStoreBuilder();

    DirectoryDocumentStoreBuilder directoryDocumentStoreBuilder();
}
