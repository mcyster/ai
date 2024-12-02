package com.cyster.ai.weave.service;

import java.io.IOException;
import java.io.InputStream;

public interface CodeInterpreterTool extends Tool<Void, Void> {

    static interface Asset {
        String getName();

        InputStream getInputStream() throws IOException;
    }

    static interface Builder {
        Builder addAsset(String name, String contents);

        Builder addAsset(Asset asest);

        CodeInterpreterTool create();
    }
}
