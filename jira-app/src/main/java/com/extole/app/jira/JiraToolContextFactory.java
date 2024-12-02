package com.extole.app.jira;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.ToolContextException;
import com.cyster.ai.weave.service.ToolContextFactory;
import com.extole.zuper.weave.ExtoleSuperContext;

@Component
public class JiraToolContextFactory implements ToolContextFactory {

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

        throw new ToolContextException("Unable to create tool context class: " + toolContextClass.getName()
                + " scenarioContext: " + scenarioContext);
    }

}
