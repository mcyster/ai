package com.cyster.ai.weave.service.tool;

import com.cyster.ai.weave.service.DocumentStore;

public interface SearchTool extends Tool<Void, Void> {

    boolean isReady();

    static interface Builder {
        Builder withName(String name);

        Builder withDocumentStore(DocumentStore store);

        SearchTool create();
    }
}
