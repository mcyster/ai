package com.cyster.store;

import org.springframework.ai.vectorstore.VectorStore;

public interface VectorStoreService {
    VectorStore getRepository(String name);
}
