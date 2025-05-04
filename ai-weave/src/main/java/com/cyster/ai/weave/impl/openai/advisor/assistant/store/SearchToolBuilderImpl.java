package com.cyster.ai.weave.impl.openai.advisor.assistant.store;

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
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.DocumentStore;
import com.cyster.ai.weave.service.DocumentStore.Document;
import com.cyster.ai.weave.service.tool.SearchTool;

import io.github.stefanbratanov.jvm.openai.CreateVectorStoreFileBatchRequest;
import io.github.stefanbratanov.jvm.openai.CreateVectorStoreRequest;
import io.github.stefanbratanov.jvm.openai.ExpiresAfter;
import io.github.stefanbratanov.jvm.openai.FilesClient;
import io.github.stefanbratanov.jvm.openai.OpenAIException;
import io.github.stefanbratanov.jvm.openai.PaginationQueryParameters;
import io.github.stefanbratanov.jvm.openai.UploadFileRequest;
import io.github.stefanbratanov.jvm.openai.VectorStore;
import io.github.stefanbratanov.jvm.openai.VectorStoreFileBatchesClient;
import io.github.stefanbratanov.jvm.openai.VectorStoresClient;
import io.github.stefanbratanov.jvm.openai.VectorStoresClient.PaginatedVectorStores;

public class SearchToolBuilderImpl<CONTEXT> implements SearchTool.Builder<CONTEXT> {
    private static final Logger logger = LoggerFactory.getLogger(SearchToolBuilderImpl.class);

    private final static String METADATA_HASH = "data_hash";

    private final OpenAiService openAiService;
    private final Class<CONTEXT> contextClass;
    private final Consumer<SearchToolImpl<CONTEXT>> searchToolResult;
    private DocumentStore documentStore;
    private String name;

    public SearchToolBuilderImpl(OpenAiService openAiService, Class<CONTEXT> contextClass,
            Consumer<SearchToolImpl<CONTEXT>> searchToolResult) {
        this.openAiService = openAiService;
        this.contextClass = contextClass;
        this.searchToolResult = searchToolResult;
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
            store = Optional.of(createStore());
        }

        var tool = useStore(store.get());

        this.searchToolResult.accept(tool);

        return tool;
    }

    private VectorStore createStore() {
        List<String> files = new ArrayList<String>();

        try {
            var directory = Files.createTempDirectory("store-" + safeName(this.name) + "-");

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

                    logger.debug("Building VectorStore " + this.name + " uploading file: " + realFile);

                    try {
                        Files.createFile(realFile);

                        document.read(inputStream -> {
                            try {
                                Files.copy(inputStream, realFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }

                            files.add(uploadFile(realFile));
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

        return vectorStore;
    }

    private String uploadFile(Path localFile) {
        int retryCount = 0;
        final int maxRetries = 5;

        String fileId = null;
        while (true) {
            try {
                var fileUpload = new UploadFileRequest(localFile, "assistants");
                var file = this.openAiService.createClient(FilesClient.class).uploadFile(fileUpload);
                fileId = file.id();
                break;
            } catch (OpenAIException exception) {
                int statusCode = exception.statusCode();

                if (statusCode >= 500 && statusCode < 600 && retryCount < maxRetries) {
                    logger.warn("Failed to upload file asset, will retry");

                    retryCount++;
                    try {
                        Thread.sleep((long) (Math.random() * 3000 + 100));
                    } catch (InterruptedException interuptExcepion) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted, while waiting to do retry on: ", exception);
                    }
                } else {
                    throw new RuntimeException("Failed to upload asset: " + localFile.toString(), exception);
                }
            }
        }

        return fileId;
    }

    private SearchToolImpl<CONTEXT> useStore(VectorStore vectorStore) {
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

        if (!vectorStore.metadata().containsKey(METADATA_HASH)) {
            return false;
        }

        if (!vectorStore.metadata().get(METADATA_HASH).equals(this.documentStore.getHash())) {
            return false;
        }

        return true;
    }

    public static boolean isVectorStoreExpired(VectorStore vectorStore) {
        long currentTimeSeconds = Instant.now().getEpochSecond();

        if (vectorStore.expiresAt() == null) {
            return false;
        }

        return currentTimeSeconds > vectorStore.expiresAt();
    }

    private static String safeName(String name) {
        return name.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_-]", "");
    }

}
