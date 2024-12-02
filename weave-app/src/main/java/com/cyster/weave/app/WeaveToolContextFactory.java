package com.cyster.weave.app;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.ToolContextException;
import com.cyster.ai.weave.service.ToolContextFactory;

@Component
public class WeaveToolContextFactory implements ToolContextFactory {

    @Override
    public <TOOL_CONTEXT, SCENARIO_CONTEXT> TOOL_CONTEXT createContext(Class<TOOL_CONTEXT> toolContextClass,
            SCENARIO_CONTEXT scenarioContext) throws ToolContextException {
        return null;
    }

}
