package com.cyster.ai.weave.impl.store;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.cyster.ai.weave.service.advisor.SearchTool;
import com.cyster.ai.weave.service.advisor.SearchTool.Document;

import io.github.stefanbratanov.jvm.openai.CreateVectorStoreFileBatchRequest;
import io.github.stefanbratanov.jvm.openai.CreateVectorStoreRequest;
import io.github.stefanbratanov.jvm.openai.ExpiresAfter;
import io.github.stefanbratanov.jvm.openai.OpenAI;
import io.github.stefanbratanov.jvm.openai.PaginationQueryParameters;
import io.github.stefanbratanov.jvm.openai.UploadFileRequest;
import io.github.stefanbratanov.jvm.openai.VectorStore;
import io.github.stefanbratanov.jvm.openai.VectorStoresClient;
import io.github.stefanbratanov.jvm.openai.VectorStoresClient.PaginatedVectorStores;

public class SearchToolBuilderImpl<CONTEXT> implements SearchTool.Builder<CONTEXT> {
    private final static String METADATA_HASH = "data_hash";
    
    private OpenAI openAi;
    private List<Document> documents = new ArrayList<Document>();
    private String name;
    private Optional<String> hash = Optional.empty();
    
    public SearchToolBuilderImpl(OpenAI openAi) {
        this.openAi = openAi;
    }

    @Override
    public SearchToolBuilderImpl<CONTEXT> withName(String name) {
        this.name = name;    
        return this;
    }

    @Override
    public SearchToolBuilderImpl<CONTEXT> withDocumentHash(String hash) {
        this.hash = Optional.of(hash);
        return this;
    }
    
    
    @Override
    public SearchToolBuilderImpl<CONTEXT> addDocument(String name, String contents) {
        this.documents.add(new StringDocument(name, contents));
        return this;
    }

    @Override
    public SearchToolBuilderImpl<CONTEXT> addDocument(File file) {
        this.documents.add(new FileDocument(file));
        return this;
    }
    
    @Override
    public SearchToolBuilderImpl<CONTEXT> addDocument(Document document) {
        this.documents.add(document);
        return this;
    }

    @Override
    public SearchTool<CONTEXT> create() {
        Optional<VectorStore> store = findVectorStore();
        if (store.isEmpty()) {
            return createStore();
        }

        return createStore(store.get());
    }
    
    public SearchTool<CONTEXT> createStore() { 
        List<String> files = new ArrayList<String>();
        
        try {
            var directory = Files.createTempDirectory("store-" + safeName(this.name));

            for(var document: this.documents) {
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
    
                Files.createFile(realFile);
                try (InputStream inputStream = document.getInputStream()) {
                    Files.copy(inputStream, realFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    
                    var fileUpload = new UploadFileRequest(realFile, "assistants");
                    var file = this.openAi.filesClient().uploadFile(fileUpload);
                    files.add(file.id());
                }
                Files.delete(realFile);
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
                var request = CreateVectorStoreRequest.newBuilder()
                        .name(this.name)
                        .fileIds(fileBatch)
                        //.metadata(null)
                        .expiresAfter(ExpiresAfter.lastActiveAt(7))
                        .build();
                    
                vectorStore = this.openAi.vectorStoresClient().createVectorStore(request);
            } else {
                var request = CreateVectorStoreFileBatchRequest.newBuilder()
                    .fileIds(fileBatch)
                    .build();
                
                this.openAi.vectorStoreFileBatchesClient().createVectorStoreFileBatch(vectorStore.id(), request);
            }
        }

        return new SearchToolImpl<CONTEXT>( new ArrayList<>(Arrays.asList(vectorStore)));
    }

    public SearchTool<CONTEXT> createStore(VectorStore vectorStore) { 
        return new SearchToolImpl<CONTEXT>( new ArrayList<>(Arrays.asList(vectorStore)));
    }
    
    private Optional<VectorStore> findVectorStore() {
        VectorStoresClient vectorStoresClient = this.openAi.vectorStoresClient();

        VectorStore newestVectorStore = null;

        PaginatedVectorStores response = null;
        do {
            PaginationQueryParameters.Builder queryBuilder = PaginationQueryParameters.newBuilder()
                .limit(99);
            if (response != null) {
                queryBuilder.after(response.lastId());
            }
            response = vectorStoresClient.listVectorStores(queryBuilder.build());

            for (var vectorStore : response.data()) {
                if (!isVectorStoreExpired(vectorStore)) {
                    if (vectorStore.name() != null && vectorStore.name().equals(this.name)) {
                        if (newestVectorStore == null || vectorStore.createdAt() > newestVectorStore.createdAt()) {
                            newestVectorStore = vectorStore;
                        }
                        
                        if (checkStoreIsLatest(vectorStore)) {
                            return Optional.of(vectorStore);
                        }
                    }
                }
            }
        } while (response.hasMore());

        return Optional.ofNullable(newestVectorStore);
    }
        
    public boolean checkStoreIsLatest(VectorStore vectorStore) {
        if (this.hash.isEmpty()) {
            this.hash = Optional.of(hashDocuments(this.documents));
        }
        
        if (vectorStore.name() == null || !vectorStore.name().equals(this.name)) {
            return false;
        }
        
        if (vectorStore.metadata().containsKey(METADATA_HASH)) {
            if (vectorStore.metadata().get(METADATA_HASH).equals(this.hash.get())) {
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
    
                try (InputStream stream = document.getInputStream()) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = stream.read(buffer)) != -1) {
                        digest.update(buffer, 0, read);
                    }
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        byte[] hashBytes = digest.digest();
        StringBuilder hashString = new StringBuilder();
        for (byte b : hashBytes) {
            hashString.append(String.format("%02x", b));
        }

        return hashString.toString();
    }
    
    private static String safeName(String name) {
        return name.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9_]", "");
    }
    
    private static class StringDocument implements Document {
        private String name;
        private String contents;
        
        public StringDocument(String name, String contents) {
            this.name = name;
            this.contents = contents;
        }
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(contents.getBytes());
        } 
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
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        } 
    }
}
