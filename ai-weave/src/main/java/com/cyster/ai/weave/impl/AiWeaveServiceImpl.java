package com.cyster.ai.weave.impl;

import com.cyster.ai.weave.impl.advisor.AdvisorService;
import com.cyster.ai.weave.impl.advisor.assistant.AdvisorServiceImpl;
import com.cyster.ai.weave.impl.assistant.AssistantScenarioBuilderImpl;
import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.impl.scenario.ScenarioSetBuilderImpl;
import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.scenario.ScenarioSetBuilder;
import com.cyster.ai.weave.service.DocumentStore.DirectoryDocumentStoreBuilder;
import com.cyster.ai.weave.service.DocumentStore.SimpleDocumentStoreBuilder;
import com.cyster.ai.weave.service.SearchTool.Builder;

public class AiWeaveServiceImpl implements AiWeaveService {
    private OpenAiService openAiService;
    private AdvisorService advisorService;
    
    public AiWeaveServiceImpl(String openAiKey) {
        this.openAiService = new OpenAiService(openAiKey);
        this.advisorService = new AdvisorServiceImpl(openAiKey);
    }
    
    @Override
    public <PARAMETERS, CONTEXT> AssistantScenarioBuilder<PARAMETERS, CONTEXT> getOrCreateAssistantScenario(
        String name) {
        // TODO get scenario if it already exists
        return new AssistantScenarioBuilderImpl<PARAMETERS, CONTEXT>(this.openAiService, name);
    }

    @Override
    public <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool) {
        return advisorService.cachingTool(tool);
    }

    @Override
    public <CONTEXT> Builder<CONTEXT> searchToolBuilder() {
        return advisorService.searchToolBuilder();
    }

    @Override
    public <CONTEXT> com.cyster.ai.weave.service.CodeInterpreterTool.Builder<CONTEXT> codeToolBuilder() {
        return advisorService.codeToolBuilder();
    }

    @Override
    public SimpleDocumentStoreBuilder simpleDocumentStoreBuilder() {
        return advisorService.simpleDocumentStoreBuilder();
    }

    @Override
    public DirectoryDocumentStoreBuilder directoryDocumentStoreBuilder() {
        return advisorService.directoryDocumentStoreBuilder();
    }

    @Override
    public ScenarioSetBuilder senarioSetBuilder() {
        return new ScenarioSetBuilderImpl();
    }

}
