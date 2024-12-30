package com.extole.app.jira;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.tool.ToolContextException;
import com.cyster.ai.weave.service.tool.ToolContextFactory;
import com.cyster.web.weave.scenarios.ManagedWebsites;
import com.cyster.web.weave.scenarios.WebsiteProvider;
import com.extole.zuper.weave.ExtoleSuperContext;

@Component
public class JiraToolContextFactory implements ToolContextFactory {
    private final String conversationLinkTemplate;
    private final WebsiteProvider websiteProvider;

    public JiraToolContextFactory(WebsiteProvider websiteProvider,
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

        if (toolContextClass == ExtoleSuperContext.class) {
            if (scenarioContext instanceof ExtoleSuperContext) {
                return (TOOL_CONTEXT) scenarioContext;
            }
        }

        if (toolContextClass == ManagedWebsites.class) {
            return (TOOL_CONTEXT) new ManagedWebsites(websiteProvider);
        }

        throw new ToolContextException("Unable to create tool context class: " + toolContextClass.getName()
                + " scenarioContext: " + scenarioContext);
    }

}