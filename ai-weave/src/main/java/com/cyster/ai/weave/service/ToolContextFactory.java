package com.cyster.ai.weave.service;

public interface ToolContextFactory {
    <TOOL_CONTEXT, SCENARIO_CONTEXT> TOOL_CONTEXT createContext(Class<TOOL_CONTEXT> toolContextClass,
            SCENARIO_CONTEXT scenarioContext) throws ToolContextException;
}
