package com.cyster.ai.weave.impl.advisor.assistant;

import com.cyster.ai.weave.impl.advisor.AdvisorBuilder;
import com.cyster.ai.weave.impl.advisor.AdvisorService;
import com.cyster.ai.weave.impl.code.CodeInterpreterToolBuilderImpl;
import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.impl.store.DirectoryDocumentStore;
import com.cyster.ai.weave.impl.store.SearchToolBuilderImpl;
import com.cyster.ai.weave.impl.store.SimpleDocumentStore;
import com.cyster.ai.weave.service.CodeInterpreterTool;
import com.cyster.ai.weave.service.DocumentStore.DirectoryDocumentStoreBuilder;
import com.cyster.ai.weave.service.DocumentStore.SimpleDocumentStoreBuilder;
import com.cyster.ai.weave.service.SearchTool;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolContextFactory;

// https://platform.openai.com/docs/assistants/overview
// https://platform.openai.com/docs/assistants/tools/code-interpreter
// https://cobusgreyling.medium.com/what-are-openai-assistant-function-tools-exactly-06ef8e39b7bd

// See
// https://platform.openai.com/assistants

public class AdvisorServiceImpl implements AdvisorService {

    private final OpenAiService openAiService;
    private final ToolContextFactory toolContextFactory;

    public AdvisorServiceImpl(String openAiKey, ToolContextFactory toolContextFactory) {
        this.openAiService = new OpenAiService(openAiKey);
        this.toolContextFactory = toolContextFactory;
    }

    public <C> AdvisorBuilder<C> getOrCreateAdvisor(String name) {
        // TODO support returning other advisor implementations: ChatAdvisor,
        // TooledChatAdvisor
        return new AssistantAdvisorImpl.Builder<C>(this.openAiService, toolContextFactory, name);
    }

    public <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool) {
        return CachingTool.builder(tool).build();
    }

    @Override
    public SearchTool.Builder searchToolBuilder() {
        return new SearchToolBuilderImpl(this.openAiService);
    }

    @Override
    public CodeInterpreterTool.Builder codeToolBuilder() {
        return new CodeInterpreterToolBuilderImpl(this.openAiService);
    }

    @Override
    public SimpleDocumentStoreBuilder simpleDocumentStoreBuilder() {
        return new SimpleDocumentStore.Builder();
    }

    @Override
    public DirectoryDocumentStoreBuilder directoryDocumentStoreBuilder() {
        return new DirectoryDocumentStore.Builder();
    }

}
