package com.cyster.ai.weave.service.tool;

import com.cyster.ai.weave.service.DocumentStore;

public interface SearchTool<CONTEXT> extends Tool<Void, CONTEXT> {

    boolean isReady();

    static interface Builder<CONTEXT> {
        Builder<CONTEXT> withName(String name);

        Builder<CONTEXT> withDocumentStore(DocumentStore store);

        SearchTool<CONTEXT> create();
    }
}
