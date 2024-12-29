package com.cyster.ai.weave.impl.store;

import java.util.Collections;
import java.util.Objects;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.SearchTool;
import com.cyster.ai.weave.service.tool.ToolException;

import io.github.stefanbratanov.jvm.openai.VectorStore;
import io.github.stefanbratanov.jvm.openai.VectorStoresClient;

public class SearchToolImpl implements SearchTool {
    public static final String NAME = "file_search";

    private OpenAiService openAiService;
    private VectorStore vectorStore;

    public SearchToolImpl(OpenAiService openAiService, VectorStore vectorStore) {
        this.openAiService = openAiService;
        this.vectorStore = vectorStore;
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
        return Objects.hash(getName(), getDescription(), getParameterClass(), vectorStore.id());
    }

    @Override
    public boolean isReady() {
        var updatedVectorStore = this.openAiService.createClient(VectorStoresClient.class)
                .retrieveVectorStore(vectorStore.id());

        return updatedVectorStore.status().equals("completed");
    }

    public VectorStore getVectorStore() {
        return this.vectorStore;
    }

}
