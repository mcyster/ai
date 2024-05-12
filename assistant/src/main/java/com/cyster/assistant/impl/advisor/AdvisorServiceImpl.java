package com.cyster.assistant.impl.advisor;


import com.cyster.assistant.service.advisor.AdvisorBuilder;
import com.cyster.assistant.service.advisor.AdvisorService;
import io.github.stefanbratanov.jvm.openai.OpenAI;

// https://platform.openai.com/docs/assistants/overview
// https://platform.openai.com/docs/assistants/tools/code-interpreter
// https://cobusgreyling.medium.com/what-are-openai-assistant-function-tools-exactly-06ef8e39b7bd

// See
// https://platform.openai.com/assistants

public class AdvisorServiceImpl implements AdvisorService {

    private final OpenAI openAi;
    
    public AdvisorServiceImpl(OpenAI openAi) {
        this.openAi = openAi;
    }
    
    public <C> AdvisorBuilder<C> getOrCreateAdvisor(String name) {
        // TODO support returning other advisor implementations: ChatAdvisor, TooledChatAdvisor
        return new AssistantAdvisorImpl.Builder<C>(this.openAi, name);    
    }
     
    
 
}
