package com.cyster.weave.impl.scenarios.tester;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.ToolException;

@Component
public class FailingTesterTool implements Tool<Void, Void> {

    FailingTesterTool() {
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Fails - to allow testing";
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
        throw new ToolException("Failing test tool - intentionally fails with this exception");
    }

}
