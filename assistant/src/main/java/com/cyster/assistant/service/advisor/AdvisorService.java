package com.cyster.assistant.service.advisor;

public interface AdvisorService { 
    <C> AdvisorBuilder<C> getOrCreateAdvisor(String name);
    
    // TODO remove use advisor
    TooledChatConversation createTooledChatConversation();


    <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool);
}
