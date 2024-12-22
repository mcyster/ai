package com.cyster.weave.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.ToolContextException;
import com.cyster.ai.weave.service.ToolContextFactory;
import com.cyster.weave.impl.scenarios.conversation.ConversationLinkTool;
import com.cyster.web.weave.scenarios.ManagedWebsites;
import com.cyster.web.weave.scenarios.WebsiteProvider;

@Component
public class WeaveToolContextFactory implements ToolContextFactory {
    private final String conversationLinkTemplate;
    private final WebsiteProvider websiteProvider;

    public WeaveToolContextFactory(WebsiteProvider websiteProvider,
            @Value("${app.url}/sites/managed/conversations/index.html?id={{conversationId}}") String conversationLinkTemplate) {
        this.conversationLinkTemplate = conversationLinkTemplate;
        this.websiteProvider = websiteProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <TOOL_CONTEXT, SCENARIO_CONTEXT> TOOL_CONTEXT createContext(Class<TOOL_CONTEXT> toolContextClass,
            SCENARIO_CONTEXT scenarioContext) throws ToolContextException {

        if (toolContextClass == Void.class) {
            return null;
        }

        if (toolContextClass == ManagedWebsites.class) {
            return (TOOL_CONTEXT) new ManagedWebsites(websiteProvider);
        }

        if (toolContextClass == ConversationLinkTool.Context.class) {
            return (TOOL_CONTEXT) new ConversationLinkTool.Context(conversationLinkTemplate);
        }

        throw new ToolContextException("Unable to create tool context class: " + toolContextClass.getName()
                + " scenarioContext: " + scenarioContext);
    }
}
