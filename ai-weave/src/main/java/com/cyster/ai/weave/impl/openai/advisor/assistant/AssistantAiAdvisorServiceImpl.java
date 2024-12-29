package com.cyster.ai.weave.impl.openai.advisor.assistant;

import com.cyster.ai.weave.impl.code.CodeInterpreterToolBuilderImpl;
import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.impl.store.SearchToolBuilderImpl;
import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.tool.CodeInterpreterTool;
import com.cyster.ai.weave.service.tool.SearchTool;
import com.cyster.ai.weave.service.tool.ToolContextFactory;

// https://platform.openai.com/docs/assistants/overview
// https://platform.openai.com/docs/assistants/tools/code-interpreter
// https://cobusgreyling.medium.com/what-are-openai-assistant-function-tools-exactly-06ef8e39b7bd

// See
// https://platform.openai.com/assistants

public class AssistantAiAdvisorServiceImpl implements AiAdvisorService {

    private final OpenAiService openAiService;
    private final ToolContextFactory toolContextFactory;

    public AssistantAiAdvisorServiceImpl(String openAiKey, ToolContextFactory toolContextFactory) {
        this.openAiService = new OpenAiService(openAiKey);
        this.toolContextFactory = toolContextFactory;
    }

    @Override
    public <CONTEXT> AdvisorBuilder<CONTEXT> getOrCreateAdvisorBuilder(String name) {
        return new AssistantAdvisorImpl.Builder<CONTEXT>(this.openAiService, toolContextFactory, name);
    }

    @Override
    public SearchTool.Builder searchToolBuilder() {
        return new SearchToolBuilderImpl(this.openAiService);
    }

    @Override
    public CodeInterpreterTool.Builder codeToolBuilder() {
        return new CodeInterpreterToolBuilderImpl(this.openAiService);
    }

}
