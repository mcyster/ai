package com.cyster.ai.weave.impl.openai;

import java.net.http.HttpClient;
import java.time.Duration;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;

import io.github.stefanbratanov.jvm.openai.AssistantsClient;
import io.github.stefanbratanov.jvm.openai.AudioClient;
import io.github.stefanbratanov.jvm.openai.BatchClient;
import io.github.stefanbratanov.jvm.openai.ChatClient;
import io.github.stefanbratanov.jvm.openai.EmbeddingsClient;
import io.github.stefanbratanov.jvm.openai.FilesClient;
import io.github.stefanbratanov.jvm.openai.FineTuningClient;
import io.github.stefanbratanov.jvm.openai.ImagesClient;
import io.github.stefanbratanov.jvm.openai.MessagesClient;
import io.github.stefanbratanov.jvm.openai.ModelsClient;
import io.github.stefanbratanov.jvm.openai.ModerationsClient;
import io.github.stefanbratanov.jvm.openai.OpenAI;
import io.github.stefanbratanov.jvm.openai.RunStepsClient;
import io.github.stefanbratanov.jvm.openai.RunsClient;
import io.github.stefanbratanov.jvm.openai.ThreadsClient;
import io.github.stefanbratanov.jvm.openai.VectorStoreFileBatchesClient;
import io.github.stefanbratanov.jvm.openai.VectorStoreFilesClient;
import io.github.stefanbratanov.jvm.openai.VectorStoresClient;

public class OpenAiService {

    private String apiKey;

    public OpenAiService() {
        this(System.getenv("OPENAI_API_KEY"));
    }

    public OpenAiService(String apiKey) {
        this.apiKey = apiKey;
    }

    public <T> T createClient(Class<T> clientClass) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

        return client(clientClass,  OpenAI.newBuilder(apiKey)
            .httpClient(httpClient)
            .build());
    }

    public <T> T createClient(Class<T> clientClass, OperationLogger operationLogger) {
        OperationLogger logger = operationLogger.childLogger(clientClass.getSimpleName());

        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

        return client(clientClass, OpenAI.newBuilder(apiKey)
            .httpClient(new HttpClientLogger(httpClient, logger))
            .build());
    }

    private <T> T client(Class<T> clientClass,   OpenAI openAiClient) {
        if (clientClass == AudioClient.class) {
            return clientClass.cast(openAiClient.audioClient());
        } else if (clientClass == ChatClient.class) {
            return clientClass.cast(openAiClient.chatClient());
        } else if (clientClass == EmbeddingsClient.class) {
            return clientClass.cast(openAiClient.embeddingsClient());
        } else if (clientClass == FineTuningClient.class) {
            return clientClass.cast(openAiClient.fineTuningClient());
        } else if (clientClass == BatchClient.class) {
            return clientClass.cast(openAiClient.batchClient());
        } else if (clientClass == FilesClient.class) {
            return clientClass.cast(openAiClient.filesClient());
        } else if (clientClass == ImagesClient.class) {
            return clientClass.cast(openAiClient.imagesClient());
        } else if (clientClass == ModelsClient.class) {
            return clientClass.cast(openAiClient.modelsClient());
        } else if (clientClass == ModerationsClient.class) {
            return clientClass.cast(openAiClient.moderationsClient());
        } else if (clientClass == AssistantsClient.class) {
            return clientClass.cast(openAiClient.assistantsClient());
        } else if (clientClass == ThreadsClient.class) {
            return clientClass.cast(openAiClient.threadsClient());
        } else if (clientClass == MessagesClient.class) {
            return clientClass.cast(openAiClient.messagesClient());
        } else if (clientClass == RunsClient.class) {
            return clientClass.cast(openAiClient.runsClient());
        } else if (clientClass == RunStepsClient.class) {
            return clientClass.cast(openAiClient.runStepsClient());
        } else if (clientClass == VectorStoresClient.class) {
            return clientClass.cast(openAiClient.vectorStoresClient());
        } else if (clientClass == VectorStoreFilesClient.class) {
            return clientClass.cast(openAiClient.vectorStoreFilesClient());
        } else if (clientClass == VectorStoreFileBatchesClient.class) {
            return clientClass.cast(openAiClient.vectorStoreFileBatchesClient());
        } else {
            throw new IllegalArgumentException("Unsupported client type: " + clientClass.getName());
        }
    }
}
