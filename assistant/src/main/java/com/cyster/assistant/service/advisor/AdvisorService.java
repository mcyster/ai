package com.cyster.assistant.service.advisor;

import com.cyster.assistant.impl.conversation.TooledChatConversation;

public interface AdvisorService { 
    <C> AdvisorBuilder<C> getOrCreateAdvisor(String name);
    
    // TODO remove use advisor
    TooledChatConversation createTooledChatConversation();


}
