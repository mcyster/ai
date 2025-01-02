package com.cyster.ai.weave.service.advisor;

import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;

public interface Advisor<CONTEXT> {

    String getName();

    ActiveConversationBuilder createConversationBuilder(CONTEXT context);

}
