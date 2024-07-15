package com.cyster.ai.weave.impl.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.cyster.ai.weave.service.advisor.DocumentStore;

public class DirectoryDocumentStore implements DocumentStore {
    private Path directory;
    private Optional<String> hash;

    private DirectoryDocumentStore(Path directory, String hash) {
        this.directory = directory;
        this.hash = Optional.ofNullable(hash);
    }

    @Override
    public String getHash() {
        if (this.hash.isEmpty()) {
            this.hash = Optional.of(hashDocuments());
        }
        return this.hash.get();
    }

    @Override
    public Stream<Document> stream() {
        Stream<Document> documents;

        try (Stream<Path> paths = Files.walk(directory)) {
           documents = paths
                .filter(Files::isRegularFile)
                .filter(path -> !hasDotInPath(path))
                .map(path -> new FileDocument(path.toFile()));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        return documents;
    }



    private static boolean hasDotInPath(Path path) {
        for (Path part : path) {
            if (part.getFileName().toString().startsWith(".")) {
                return true;
            }
        }
        return false;
    }

    public String hashDocuments() {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }

        this.stream().forEach(document -> {
            digest.update(document.getName().getBytes());

            try {
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
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });

        byte[] hashBytes = digest.digest();
        StringBuilder hashString = new StringBuilder();
        for (byte value : hashBytes) {
            hashString.append(String.format("%02x", value));
        }

        return hashString.toString();
    }

    private static class FileDocument implements Document {
        private File file;

        public FileDocument(File file) {
            this.file = file;
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public void read(Consumer<InputStream> stream) throws IOException {
            try (InputStream inputStream = new FileInputStream(file)) {
                stream.accept(inputStream);
            }
        }
    }

    public static class Builder implements DirectoryDocumentStoreBuilder {
        private Path directory = null;
        private String hash = null;


        @Override
        public DirectoryDocumentStoreBuilder withDirectory(Path directory) {
            this.directory = directory;
            return this;
        }

        @Override
        public Builder withHash(String hash) {
            this.hash = hash;
            return this;
        }

        @Override
        public DirectoryDocumentStore create() {
            return new DirectoryDocumentStore(this.directory, this.hash);
        }


    }
}
