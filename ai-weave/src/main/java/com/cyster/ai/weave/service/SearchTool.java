package com.cyster.ai.weave.service;

public interface SearchTool<CONTEXT> extends Tool<Void, CONTEXT> {

    static interface Builder<CONTEXT> {
        Builder<CONTEXT> withName(String name);
        Builder<CONTEXT> withDocumentStore(DocumentStore store);

        SearchTool<CONTEXT> create();
    }
}
