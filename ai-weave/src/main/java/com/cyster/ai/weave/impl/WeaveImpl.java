package com.cyster.ai.weave.impl;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.ActiveConversation;

public record WeaveImpl(ActiveConversation conversation, WeaveOperation operation) implements Weave {
}
