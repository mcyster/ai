package com.cyster.ai.weave.impl.openai.advisor.assistant.store;

import java.util.Collections;
import java.util.Objects;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.SearchTool;
import com.cyster.ai.weave.service.tool.ToolException;

import io.github.stefanbratanov.jvm.openai.VectorStore;
import io.github.stefanbratanov.jvm.openai.VectorStoresClient;

public class SearchToolImpl<CONTEXT> implements SearchTool<CONTEXT> {
    public static final String NAME = "file_search";

    private final OpenAiService openAiService;
    private final VectorStore vectorStore;
    private final Class<CONTEXT> contextClass;

    public SearchToolImpl(OpenAiService openAiService, VectorStore vectorStore, Class<CONTEXT> contextClass) {
        this.openAiService = openAiService;
        this.vectorStore = vectorStore;
        this.contextClass = contextClass;
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
    public Class<CONTEXT> getContextClass() {
        return contextClass;
    }

    @Override
    public Object execute(Void parameters, CONTEXT context, Weave weave) throws ToolException {
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
