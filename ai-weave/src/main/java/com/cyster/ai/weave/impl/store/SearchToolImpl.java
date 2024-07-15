package com.cyster.ai.weave.impl.store;

import java.util.Collections;
import java.util.List;

import io.github.stefanbratanov.jvm.openai.VectorStore;

import com.cyster.ai.weave.service.advisor.SearchTool;
import com.cyster.ai.weave.service.advisor.ToolException;

public class SearchToolImpl<CONTEXT> implements SearchTool<CONTEXT> {
    public static final String NAME = "file_search";

    private List<VectorStore> vectorStores;

    public SearchToolImpl(List<VectorStore> vectorStores) {
        this.vectorStores = vectorStores;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Search and return the best associated document";
    }

    @Override
    public Class<Void> getParameterClass() {
        return null;
    }

    @Override
    public Object execute(Void parameters, CONTEXT context) throws ToolException {
        // Implemented directly by OpenAI
        return Collections.emptyMap();
    }

    public List<VectorStore> getVectorStores() {
        return this.vectorStores;
    }
}
