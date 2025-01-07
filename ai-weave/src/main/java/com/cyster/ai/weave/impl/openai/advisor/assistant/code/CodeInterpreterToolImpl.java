package com.cyster.ai.weave.impl.openai.advisor.assistant.code;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.CodeInterpreterTool;
import com.cyster.ai.weave.service.tool.ToolException;

public class CodeInterpreterToolImpl<CONTEXT> implements CodeInterpreterTool<CONTEXT> {
    public static final String NAME = "code_interpreter";

    private List<String> fileIds;
    private Class<CONTEXT> contextClass;

    public CodeInterpreterToolImpl(List<String> fileIds, Class<CONTEXT> contextClass) {
        this.fileIds = fileIds;
        this.contextClass = contextClass;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Runs python code";
    }

    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Class<CONTEXT> getContextClass() {
        return contextClass;
    }

    @Override
    public Object execute(Void parameters, CONTEXT context, Weave weave) throws ToolException {
        // Implemented directly by OpenAI
        return Collections.emptyMap();
    }

    public int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass(), fileIds);
    }

    public List<String> getFileIds() {
        return this.fileIds;
    }

}
