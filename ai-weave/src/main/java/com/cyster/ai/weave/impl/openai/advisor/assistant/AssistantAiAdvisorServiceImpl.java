package com.cyster.ai.weave.impl.openai.advisor.assistant;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;

// https://platform.openai.com/docs/assistants/overview
// https://platform.openai.com/docs/assistants/tools/code-interpreter
// https://cobusgreyling.medium.com/what-are-openai-assistant-function-tools-exactly-06ef8e39b7bd

// See
// https://platform.openai.com/assistants

public class AssistantAiAdvisorServiceImpl implements AiAdvisorService {

    private final OpenAiService openAiService;

    public AssistantAiAdvisorServiceImpl(String openAiKey) {
        this.openAiService = new OpenAiService(openAiKey);
    }

    @Override
    public <CONTEXT> AdvisorBuilder<CONTEXT> getOrCreateAdvisorBuilder(String name) {
        return new AssistantAdvisorImpl.Builder<CONTEXT>(this.openAiService, name);
    }

}
