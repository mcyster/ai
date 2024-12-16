package com.cyster.ai.weave.service.conversation;

import java.util.List;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.conversation.Message.Type;

public interface AdvisorConversation extends Conversation {

    String id();

    Message addMessage(Type type, String message);

    Message respond() throws ConversationException;

    Message respond(OperationLogger operation) throws ConversationException;

    List<Message> messages();
}
