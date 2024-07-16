package com.cyster.ai.weave.impl.advisor;

import com.cyster.ai.weave.service.CodeInterpreterTool;
import com.cyster.ai.weave.service.SearchTool;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.DocumentStore.DirectoryDocumentStoreBuilder;
import com.cyster.ai.weave.service.DocumentStore.SimpleDocumentStoreBuilder;

public interface AdvisorService {
    <C> AdvisorBuilder<C> getOrCreateAdvisor(String name);

    <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool);

    <CONTEXT> SearchTool.Builder<CONTEXT> searchToolBuilder();
    <CONTEXT> CodeInterpreterTool.Builder<CONTEXT> codeToolBuilder();

    SimpleDocumentStoreBuilder simpleDocumentStoreBuilder();
    DirectoryDocumentStoreBuilder directoryDocumentStoreBuilder();

}
