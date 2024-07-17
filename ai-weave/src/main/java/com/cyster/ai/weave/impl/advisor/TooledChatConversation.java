package com.cyster.ai.weave.impl.advisor;

import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.conversation.Conversation;

// TODO TooledChatAdvisor is the generic form of this - remove one
public interface TooledChatConversation extends Conversation {

    TooledChatConversation addUserMessage(String content);

    TooledChatConversation addSystemMessage(String content) ;

    TooledChatConversation addAiMessage(String content);

    <T> TooledChatConversation addTool(Tool<T, Void> tool);

}