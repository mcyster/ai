package com.cyster.weave.impl.scenarios.conversation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.weave.impl.scenarios.conversation.ConversationLinkTool.Context;

@Component
public class ConversationLinkTool implements Tool<Void, Context> {

    ConversationLinkTool() {
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Link to the current conversation";
    }

    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Class<Context> getContextClass() {
        return Context.class;
    }

    @Override
    public Object execute(Void parameters, Context context, OperationLogger operation) throws ToolException {
        Map<String, String> response = new HashMap<>() {
            {
                put("id", context.conversationId);
                put("link", context.conversationLink);
            }
        };

        return response;
    }

    public static record Context(String conversationId, String conversationLink) {
    };

}
