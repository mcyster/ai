package com.extole.app.jira;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.ToolContextException;
import com.cyster.ai.weave.service.ToolContextFactory;
import com.cyster.template.StringTemplate;
import com.cyster.weave.impl.scenarios.conversation.ConversationLinkTool;
import com.extole.zuper.weave.ExtoleSuperContext;

@Component
public class JiraToolContextFactory implements ToolContextFactory {
    private final String conversationLinkTemplate;

    public JiraToolContextFactory(
            @Value("${extoleConversationPageTemplate:https://beep-boop.extole.com/sites/managed/conversations/index.html?id={{conversationId}}}") String conversationLinkTemplate) {
        this.conversationLinkTemplate = conversationLinkTemplate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TOOL_CONTEXT, SCENARIO_CONTEXT> TOOL_CONTEXT createContext(Class<TOOL_CONTEXT> toolContextClass,
            SCENARIO_CONTEXT scenarioContext) throws ToolContextException {

        if (toolContextClass == Void.class) {
            return null;
        }

        if (toolContextClass == ExtoleSuperContext.class) {
            if (scenarioContext instanceof ExtoleSuperContext) {
                return (TOOL_CONTEXT) scenarioContext;
            }
        }

        if (toolContextClass == ConversationLinkTool.Context.class) {
            if (scenarioContext instanceof ExtoleSuperContext) {
                String id = ((ExtoleSuperContext) scenarioContext).conversionId();

                Map<String, Object> parameters = new HashMap<>() {
                    {
                        put("conversationId", id);
                    }
                };

                String link = new StringTemplate(conversationLinkTemplate).render(parameters);

                return (TOOL_CONTEXT) new ConversationLinkTool.Context(id, link);
            }
        }

        throw new ToolContextException("Unable to create tool context class: " + toolContextClass.getName()
                + " scenarioContext: " + scenarioContext);
    }

}
