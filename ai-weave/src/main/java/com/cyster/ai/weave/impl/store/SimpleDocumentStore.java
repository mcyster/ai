package com.cyster.ai.weave.impl.store;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.cyster.ai.weave.service.advisor.DocumentStore;

public class SimpleDocumentStore implements DocumentStore {
    List<Document> documents;
    Optional<String> hash = Optional.empty();

    private SimpleDocumentStore(List<Document> documents) {
        this.documents = documents;
    }

    @Override
    public String getHash() {
        if (hash.isEmpty()) {
            hash = Optional.of(hashDocuments(this.documents));
        }

        return this.hash.get();
    }

    @Override
    public Stream<Document> stream() {
        return documents.stream();
    }

    public static String hashDocuments(List<Document> documents) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }

        try {
            for (Document document : documents) {
                digest.update(document.getName().getBytes());

                document.read(inputStream -> {
                    byte[] buffer = new byte[1024];
                    int read;
                    try {
                        while ((read = inputStream.read(buffer)) != -1) {
                            digest.update(buffer, 0, read);
                        }
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        byte[] hashBytes = digest.digest();
        StringBuilder hashString = new StringBuilder();
        for (byte value : hashBytes) {
            hashString.append(String.format("%02x", value));
        }

        return hashString.toString();
    }

    static class SimpleDocument implements Document {
        private final String name;
        private final String content;

        SimpleDocument(String name, String content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public void read(Consumer<InputStream> stream) throws IOException {
            try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
                stream.accept(inputStream);
            }
        }
    }

    public static class Builder implements SimpleDocumentStoreBuilder {
        private List<Document> documents = new ArrayList<>();

        @Override
        public Builder addDocument(String name, String content) {
            documents.add(new SimpleDocument(name, content));
            return this;
        }

        @Override
        public SimpleDocumentStore create() {
            return new SimpleDocumentStore(documents);
        }

    }
}
