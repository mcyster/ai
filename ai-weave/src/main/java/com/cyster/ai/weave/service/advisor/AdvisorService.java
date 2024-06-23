package com.cyster.ai.weave.service.advisor;

import com.cyster.ai.weave.service.advisor.DocumentStore.DirectoryDocumentStoreBuilder;
import com.cyster.ai.weave.service.advisor.DocumentStore.SimpleDocumentStoreBuilder;

public interface AdvisorService { 
    <C> AdvisorBuilder<C> getOrCreateAdvisor(String name);
    
    <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool);

    <CONTEXT> SearchTool.Builder<CONTEXT> searchToolBuilder();
    <CONTEXT> CodeInterpreterTool.Builder<CONTEXT> codeToolBuilder();
    
    SimpleDocumentStoreBuilder simpleDocumentStoreBuilder();
    DirectoryDocumentStoreBuilder directoryDocumentStoreBuilder();

}
