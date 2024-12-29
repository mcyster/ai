package com.cyster.ai.weave.service;

import com.cyster.ai.weave.service.DocumentStore.DirectoryDocumentStoreBuilder;
import com.cyster.ai.weave.service.DocumentStore.SimpleDocumentStoreBuilder;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.ToolException;

public interface AiService {

    <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool);

    SimpleDocumentStoreBuilder simpleDocumentStoreBuilder();

    DirectoryDocumentStoreBuilder directoryDocumentStoreBuilder();

    String getJsonSchema(Class<?> clazz);

    <RESPONSE> RESPONSE extractResponse(Class<RESPONSE> response, String input) throws ToolException;
}
