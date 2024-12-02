package com.cyster.ai.weave.service;

public interface SearchTool extends Tool<Void, Void> {

    boolean isReady();

    static interface Builder {
        Builder withName(String name);

        Builder withDocumentStore(DocumentStore store);

        SearchTool create();
    }
}
