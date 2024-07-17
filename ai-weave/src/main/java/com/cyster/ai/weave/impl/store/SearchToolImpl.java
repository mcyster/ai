package com.cyster.ai.weave.impl.store;

import java.util.Collections;

import io.github.stefanbratanov.jvm.openai.VectorStore;
import io.github.stefanbratanov.jvm.openai.VectorStoresClient;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.SearchTool;
import com.cyster.ai.weave.service.ToolException;

public class SearchToolImpl<CONTEXT> implements SearchTool<CONTEXT> {
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
        return null;
    }

    @Override
    public Object execute(Void parameters, CONTEXT context) throws ToolException {
        // Implemented directly by OpenAI
        return Collections.emptyMap();
    }

    @Override
    public boolean isReady() {
        var updatedVectorStore = this.openAiService.createClient(VectorStoresClient.class).retrieveVectorStore(vectorStore.id());
                
        return updatedVectorStore.status().equals("completed");
    }
    
    public VectorStore getVectorStore() {
        return this.vectorStore;
    }
}
