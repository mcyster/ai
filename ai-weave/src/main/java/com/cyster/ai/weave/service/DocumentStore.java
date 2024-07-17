package com.cyster.ai.weave.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface DocumentStore {
    String getHash();
    Stream<Document> stream();

    public static interface Document {
      String getName();
      void read(Consumer<InputStream> stream) throws IOException;
    }

    public static interface SimpleDocumentStoreBuilder {
        SimpleDocumentStoreBuilder addDocument(String name, String content);
        DocumentStore create();
    }

    public static interface DirectoryDocumentStoreBuilder {
        DirectoryDocumentStoreBuilder withDirectory(Path directory);
        DirectoryDocumentStoreBuilder withHash(String hash);
        DocumentStore create();
    }
}
