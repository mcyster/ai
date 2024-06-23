package com.cyster.ai.weave.service.conversation;

import java.util.List;

import com.cyster.ai.weave.service.conversation.Message.Type;

public interface Conversation {

    Message addMessage(Type type, String message);
    
    Message respond() throws ConversationException;

    List<Message> getMessages();
}
