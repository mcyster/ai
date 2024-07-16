package com.cyster.ai.weave.service;

import com.cyster.ai.weave.service.DocumentStore.DirectoryDocumentStoreBuilder;
import com.cyster.ai.weave.service.DocumentStore.SimpleDocumentStoreBuilder;

// TBD should project / this just be an AiScenarioService

public interface AiWeaveService {
    <PARAMETERS, CONTEXT> AssistantScenarioBuilder<PARAMETERS, CONTEXT> getOrCreateAssistantScenario(String name);
    
    <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool);

    <CONTEXT> SearchTool.Builder<CONTEXT> searchToolBuilder();
    <CONTEXT> CodeInterpreterTool.Builder<CONTEXT> codeToolBuilder();

    SimpleDocumentStoreBuilder simpleDocumentStoreBuilder();
    DirectoryDocumentStoreBuilder directoryDocumentStoreBuilder();
}
