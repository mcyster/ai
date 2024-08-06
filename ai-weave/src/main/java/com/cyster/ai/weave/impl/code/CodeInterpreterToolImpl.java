package com.cyster.ai.weave.impl.code;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.CodeInterpreterTool;
import com.cyster.ai.weave.service.ToolException;

public class CodeInterpreterToolImpl<CONTEXT> implements CodeInterpreterTool<CONTEXT> {
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
        return null;
    }

    @Override
    public Object execute(Void parameters, CONTEXT context, OperationLogger operation) throws ToolException {
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

