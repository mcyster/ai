package com.cyster.ai.weave.service.tool;

import com.cyster.ai.weave.service.Weave;

public class VoidToolAdapter<PARAMETERS, CONTEXT> implements Tool<PARAMETERS, CONTEXT> {
    private Tool<PARAMETERS, Void> tool;
    private Class<CONTEXT> contextClass;

    public VoidToolAdapter(Tool<PARAMETERS, Void> tool, Class<CONTEXT> contextClass) {
        this.tool = tool;
        this.contextClass = contextClass;
    }

    @Override
    public String getName() {
        return tool.getName();
    }

    @Override
    public String getDescription() {
        return tool.getDescription();
    }

    @Override
    public Class<PARAMETERS> getParameterClass() {
        return tool.getParameterClass();
    }

    @Override
    public Class<CONTEXT> getContextClass() {
        return contextClass;
    }

    @Override
    public Object execute(PARAMETERS parameters, CONTEXT context, Weave weave) throws ToolException {
        return tool.execute(parameters, null, weave);
    }

}
