package com.cyster.weave.impl.scenarios.conversation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.Weave;
import com.cyster.template.StringTemplate;
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
    public Object execute(Void parameters, Context context, Weave weave) throws ToolException {

        var template = new StringTemplate(context.conversationLinkTemplate);
        var templateParameters = new HashMap<>() {
            {
                put("id", weave.conversation().id());
            }
        };
        var link = template.render(templateParameters);

        Map<String, String> response = new HashMap<>() {
            {
                put("id", weave.conversation().id());
                put("link", link);
            }
        };

        return response;
    }

    public static record Context(String conversationLinkTemplate) {
    };

}
