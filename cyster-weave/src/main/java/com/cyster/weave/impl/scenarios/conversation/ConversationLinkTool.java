package com.cyster.weave.impl.scenarios.conversation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.ToolException;
import com.cyster.template.StringTemplate;

@Component
public class ConversationLinkTool implements Tool<Void, Void> {
    private String conversationLinkTemplate;

    ConversationLinkTool() {
        this.conversationLinkTemplate = "TODO initialize";
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
    public Object execute(Void parameters, Void context, Weave weave) throws ToolException {

        var template = new StringTemplate(conversationLinkTemplate);
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

}
