package com.cyster.ai.weave.service.advisor;

import java.io.IOException;
import java.io.InputStream;

public interface CodeInterpreterTool<CONTEXT> extends Tool<Void, CONTEXT> {

    static interface Asset {
        String getName();
        InputStream getInputStream() throws IOException;
    }

    static interface Builder<CONTEXT> {
        Builder<CONTEXT> addAsset(String name, String contents);
        Builder<CONTEXT> addAsset(Asset asest);
        CodeInterpreterTool<CONTEXT> create();
    }
}
