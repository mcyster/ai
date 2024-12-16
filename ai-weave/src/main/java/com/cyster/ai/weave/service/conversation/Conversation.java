package com.cyster.ai.weave.service.conversation;

import java.util.List;

public interface Conversation {

    String id();

    List<Message> messages();

}
