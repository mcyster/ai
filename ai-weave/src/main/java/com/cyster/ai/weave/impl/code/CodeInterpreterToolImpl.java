package com.cyster.ai.weave.impl.code;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.cyster.ai.weave.service.CodeInterpreterTool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.Weave;

public class CodeInterpreterToolImpl implements CodeInterpreterTool {
    public static final String NAME = "code_interpreter";

    private List<String> fileIds;

    public CodeInterpreterToolImpl(List<String> fileIds) {
        this.fileIds = fileIds;
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
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Object execute(Void parameters, Void context, Weave weave) throws ToolException {
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
