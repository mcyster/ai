package com.cyster.ai.weave.impl;

import com.cyster.ai.weave.impl.assistant.AssistantScenarioBuilderImpl;
import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;

public class AiWeaveServiceImpl implements AiWeaveService {
    private OpenAiService openAiService;
    
    public AiWeaveServiceImpl(String openAiKey) {
        this.openAiService = new OpenAiService(openAiKey);
    }
    
    @Override
    public <PARAMETERS, CONTEXT> AssistantScenarioBuilder<PARAMETERS, CONTEXT> getOrCreateAssistantScenario(
        String name) {
        // TODO get scenario if it already exists
        return new AssistantScenarioBuilderImpl<PARAMETERS, CONTEXT>(this.openAiService, name);
    }

}
