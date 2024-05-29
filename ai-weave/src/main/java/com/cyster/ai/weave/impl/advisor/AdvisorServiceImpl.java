package com.cyster.ai.weave.impl.advisor;


import java.net.http.HttpClient;
import java.time.Duration;

import com.cyster.ai.weave.impl.code.CodeInterpreterToolBuilderImpl;
import com.cyster.ai.weave.impl.conversation.TooledChatConversationImpl;
import com.cyster.ai.weave.impl.store.SearchToolBuilderImpl;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.advisor.AdvisorServiceFactory;
import com.cyster.ai.weave.service.advisor.CodeInterpreterTool;
import com.cyster.ai.weave.service.advisor.SearchTool;
import com.cyster.ai.weave.service.advisor.Tool;
import com.cyster.ai.weave.service.advisor.TooledChatConversation;

import io.github.stefanbratanov.jvm.openai.OpenAI;

// https://platform.openai.com/docs/assistants/overview
// https://platform.openai.com/docs/assistants/tools/code-interpreter
// https://cobusgreyling.medium.com/what-are-openai-assistant-function-tools-exactly-06ef8e39b7bd

// See
// https://platform.openai.com/assistants

public class AdvisorServiceImpl implements AdvisorService {

    private final OpenAI openAi;
    
    public AdvisorServiceImpl(String openAiKey) {
        this.openAi = createOpenAiService(openAiKey, true);
    }
    
    public <C> AdvisorBuilder<C> getOrCreateAdvisor(String name) {
        // TODO support returning other advisor implementations: ChatAdvisor, TooledChatAdvisor
        return new AssistantAdvisorImpl.Builder<C>(this.openAi, name);    
    }
     
    // TBD is this an advisor ??? 
    public TooledChatConversation createTooledChatConversation() {
        return new TooledChatConversationImpl(this.openAi);
    }
    
    public <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool) {
        return CachingTool.builder(tool).build();
    }

    @Override
    public <CONTEXT> SearchTool.Builder<CONTEXT> searchToolBuilder() {
        return new SearchToolBuilderImpl<CONTEXT>(this.openAi);
    }

    @Override
    public <CONTEXT> CodeInterpreterTool.Builder<CONTEXT> codeToolBuilder() {
        return new CodeInterpreterToolBuilderImpl<CONTEXT>(this.openAi);
    }
   
    private static OpenAI createOpenAiService(String openApiKey, Boolean debug) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
        
        // TODO support logging request / response   
     
        return OpenAI.newBuilder(System.getenv("OPENAI_API_KEY"))
                .httpClient(httpClient)
                .build();             
    }
    
    public static class Factory implements AdvisorServiceFactory {
        public Factory() {    
        }
        
        @Override
        public AdvisorService createAdvisorService(String openAiApiKey) {
            return new AdvisorServiceImpl(openAiApiKey);
        }
    }


}
