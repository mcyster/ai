package com.extole.app.admin;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.tool.ToolContextException;
import com.cyster.ai.weave.service.tool.ToolContextFactory;
import com.extole.admin.weave.session.ExtoleSessionContext;

@Component
public class ExtoleAdminToolContextFactory implements ToolContextFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <TOOL_CONTEXT, SCENARIO_CONTEXT> TOOL_CONTEXT createContext(Class<TOOL_CONTEXT> toolContextClass,
            SCENARIO_CONTEXT scenarioContext) throws ToolContextException {
        if (toolContextClass == Void.class) {
            return null;
        }
        if (toolContextClass == ExtoleSessionContext.class) {
            if (scenarioContext instanceof ExtoleSessionContext) {
                return (TOOL_CONTEXT) scenarioContext;
            }
        }

        throw new ToolContextException("Unable to create tool context class: " + toolContextClass.getName());
    }

}
