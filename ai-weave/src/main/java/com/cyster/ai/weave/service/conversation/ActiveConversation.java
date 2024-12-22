package com.cyster.ai.weave.service.conversation;

import java.util.List;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.Message.Type;

public interface ActiveConversation extends Conversation {

    Message addMessage(Type type, String message);

    Message respond() throws ConversationException;

    Message respond(Weave weave) throws ConversationException;

    List<Message> messages();
}
