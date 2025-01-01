package com.cyster.weave.app;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.tool.ToolContextException;
import com.cyster.ai.weave.service.tool.ToolContextFactory;
import com.cyster.web.weave.scenarios.ManagedWebsites;
import com.cyster.web.weave.scenarios.WebsiteProvider;

@Component
public class WeaveToolContextFactory implements ToolContextFactory {
    private final WebsiteProvider websiteProvider;

    public WeaveToolContextFactory(WebsiteProvider websiteProvider) {
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

        throw new ToolContextException("Unable to create tool context class: " + toolContextClass.getName()
                + " scenarioContext: " + scenarioContext);
    }
}
