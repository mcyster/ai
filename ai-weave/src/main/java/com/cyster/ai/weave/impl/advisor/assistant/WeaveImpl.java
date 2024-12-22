package com.cyster.ai.weave.impl.advisor.assistant;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.Conversation;

public record WeaveImpl(Conversation conversation, WeaveOperation operation) implements Weave {
}
