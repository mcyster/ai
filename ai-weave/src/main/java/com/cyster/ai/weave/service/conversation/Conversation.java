package com.cyster.ai.weave.service.conversation;

import java.util.List;

public interface Conversation {

    Message respond() throws ConversationException;

    Message respond(String message) throws ConversationException;

    List<Message> getMessages();
}
