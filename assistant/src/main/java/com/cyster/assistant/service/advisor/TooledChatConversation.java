package com.cyster.assistant.service.advisor;

import com.cyster.assistant.service.conversation.Conversation;

// TODO TooledChatAdvisor is the generic form of this - remove one
public interface TooledChatConversation extends Conversation {

    public TooledChatConversation addUserMessage(String content);
    
    public TooledChatConversation addSystemMessage(String content) ;
    
    public TooledChatConversation addAiMessage(String content);
    
    public <T> TooledChatConversation addTool(Tool<T, Void> tool);

}