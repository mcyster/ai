package com.cyster.weave.impl.scenarios.conversation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;

@Component
public class ConversationLinkTool implements Tool<Void, Void> {

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
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Object execute(Void parameters, Void context, OperationLogger operation) throws ToolException {
        Map<String, String> response = new HashMap<>() {
            {
                put("url", "http://cyster.com/123");
            }
        };

        return response;
    }

}
