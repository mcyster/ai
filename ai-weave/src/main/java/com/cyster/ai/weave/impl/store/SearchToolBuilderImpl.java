package com.cyster.ai.weave.impl.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.DocumentStore;
import com.cyster.ai.weave.service.DocumentStore.Document;
import com.cyster.ai.weave.service.tool.SearchTool;

import io.github.stefanbratanov.jvm.openai.CreateVectorStoreFileBatchRequest;
import io.github.stefanbratanov.jvm.openai.CreateVectorStoreRequest;
import io.github.stefanbratanov.jvm.openai.ExpiresAfter;
import io.github.stefanbratanov.jvm.openai.FilesClient;
import io.github.stefanbratanov.jvm.openai.PaginationQueryParameters;
import io.github.stefanbratanov.jvm.openai.UploadFileRequest;
import io.github.stefanbratanov.jvm.openai.VectorStore;
import io.github.stefanbratanov.jvm.openai.VectorStoreFileBatchesClient;
import io.github.stefanbratanov.jvm.openai.VectorStoresClient;
import io.github.stefanbratanov.jvm.openai.VectorStoresClient.PaginatedVectorStores;

public class SearchToolBuilderImpl<CONTEXT> implements SearchTool.Builder<CONTEXT> {
    private final static String METADATA_HASH = "data_hash";

    private OpenAiService openAiService;
    private Class<CONTEXT> contextClass;
    private DocumentStore documentStore;
    private String name;

    public SearchToolBuilderImpl(OpenAiService openAiService, Class<CONTEXT> contextClass) {
        this.openAiService = openAiService;
        this.contextClass = contextClass;
    }

    @Override
    public SearchToolBuilderImpl<CONTEXT> withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public SearchToolBuilderImpl<CONTEXT> withDocumentStore(DocumentStore documentStore) {
        this.documentStore = documentStore;
        return this;
    }

    @Override
    public SearchTool<CONTEXT> create() {
        Optional<VectorStore> store = findVectorStore();
        if (store.isEmpty()) {
            return createStore();
        }

        return useStore(store.get());
    }

    public SearchTool<CONTEXT> createStore() {
        List<String> files = new ArrayList<String>();

        try {
            var directory = Files.createTempDirectory("store-" + safeName(this.name));

            try (Stream<Document> documentStream = documentStore.stream()) {
                documentStream.forEach(document -> {
                    var name = document.getName();
                    var extension = ".txt";

                    int lastDotIndex = name.lastIndexOf('.');
                    if (lastDotIndex != -1) {
                        extension = name.substring(lastDotIndex + 1);
                        name = name.substring(0, lastDotIndex);
                    }

                    var safeName = safeName(name);
                    var safeExtension = "." + safeName(extension);

                    Path realFile = Paths.get(directory.toString(), safeName + safeExtension);

                    try {
                        Files.createFile(realFile);

                        document.read(inputStream -> {
                            try {
                                Files.copy(inputStream, realFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }

                            var fileUpload = new UploadFileRequest(realFile, "assistants");
                            var file = this.openAiService.createClient(FilesClient.class).uploadFile(fileUpload);
                            files.add(file.id());
                        });
                        Files.delete(realFile);

                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }

            if (directory != null) {
                Files.delete(directory);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        VectorStore vectorStore = null;

        int totalFileCount = files.size();
        int batchSize = 100;
        for (int i = 0; i < totalFileCount; i += batchSize) {
            int end = Math.min(totalFileCount, i + batchSize);
            List<String> fileBatch = files.subList(i, end);
            if (vectorStore == null) {
                Map<String, String> metadata = new HashMap<>() {
                    {
                        put(METADATA_HASH, documentStore.getHash());
                    }
                };

                var request = CreateVectorStoreRequest.newBuilder().name(this.name).fileIds(fileBatch)
                        .metadata(metadata).expiresAfter(ExpiresAfter.lastActiveAt(7)).build();

                vectorStore = this.openAiService.createClient(VectorStoresClient.class).createVectorStore(request);
            } else {
                var request = CreateVectorStoreFileBatchRequest.newBuilder().fileIds(fileBatch).build();

                this.openAiService.createClient(VectorStoreFileBatchesClient.class)
                        .createVectorStoreFileBatch(vectorStore.id(), request);
            }
        }

        return new SearchToolImpl<CONTEXT>(this.openAiService, vectorStore, this.contextClass);
    }

    public SearchTool<CONTEXT> useStore(VectorStore vectorStore) {
        return new SearchToolImpl<CONTEXT>(this.openAiService, vectorStore, this.contextClass);
    }

    private Optional<VectorStore> findVectorStore() {
        VectorStoresClient vectorStoresClient = this.openAiService.createClient(VectorStoresClient.class);

        PaginatedVectorStores response = null;
        do {
            PaginationQueryParameters.Builder queryBuilder = PaginationQueryParameters.newBuilder().limit(99);
            if (response != null) {
                queryBuilder.after(response.lastId());
            }
            response = vectorStoresClient.listVectorStores(queryBuilder.build());

            for (var vectorStore : response.data()) {
                if (!isVectorStoreExpired(vectorStore)) {
                    if (checkStoreMatches(vectorStore)) {
                        return Optional.of(vectorStore);
                    }
                }
            }
        } while (response.hasMore());

        return Optional.empty();
    }

    public boolean checkStoreMatches(VectorStore vectorStore) {
        if (vectorStore.name() == null || !vectorStore.name().equals(this.name)) {
            return false;
        }

        if (vectorStore.metadata().containsKey(METADATA_HASH)) {
            if (vectorStore.metadata().get(METADATA_HASH).equals(this.documentStore.getHash())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isVectorStoreExpired(VectorStore vectorStore) {
        long currentTimeSeconds = Instant.now().getEpochSecond();

        if (vectorStore.expiresAt() == null) {
            return false;
        }

        return currentTimeSeconds > vectorStore.expiresAt();
    }

    private static String safeName(String name) {
        return name.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "");
    }

}
