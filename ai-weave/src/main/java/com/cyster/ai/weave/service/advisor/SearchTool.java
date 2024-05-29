package com.cyster.ai.weave.service.advisor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface SearchTool<CONTEXT> extends Tool<Void, CONTEXT> {
        
    static interface Document {
        String getName();
        InputStream getInputStream() throws IOException;
    }
    
    static interface Builder<CONTEXT> {
        Builder<CONTEXT> withName(String name);
        Builder<CONTEXT> addDocument(String name, String contents);
        Builder<CONTEXT> addDocument(File file);
        Builder<CONTEXT> addDocument(Document document);
        
        SearchTool<CONTEXT> create();
    }
}
