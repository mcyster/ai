package com.cyster.ai.weave.impl.openai;

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

public class OpenAiClient {

    private OpenAI openAi;
    
    public OpenAiClient(OpenAI openAi) {
        this.openAi = openAi;
    }
 
    public <T> T client(Class<T> clientClass) {
        if (clientClass == AudioClient.class) {
            return clientClass.cast(openAi.audioClient());
        } else if (clientClass == ChatClient.class) {
            return clientClass.cast(openAi.chatClient());
        } else if (clientClass == EmbeddingsClient.class) {
            return clientClass.cast(openAi.embeddingsClient());
        } else if (clientClass == FineTuningClient.class) {
            return clientClass.cast(openAi.fineTuningClient());
        } else if (clientClass == BatchClient.class) {
            return clientClass.cast(openAi.batchClient());
        } else if (clientClass == FilesClient.class) {
            return clientClass.cast(openAi.filesClient());
        } else if (clientClass == ImagesClient.class) {
            return clientClass.cast(openAi.imagesClient());
        } else if (clientClass == ModelsClient.class) {
            return clientClass.cast(openAi.modelsClient());
        } else if (clientClass == ModerationsClient.class) {
            return clientClass.cast(openAi.moderationsClient());
        } else if (clientClass == AssistantsClient.class) {
            return clientClass.cast(openAi.assistantsClient());
        } else if (clientClass == ThreadsClient.class) {
            return clientClass.cast(openAi.threadsClient());
        } else if (clientClass == MessagesClient.class) {
            return clientClass.cast(openAi.messagesClient());
        } else if (clientClass == RunsClient.class) {
            return clientClass.cast(openAi.runsClient());
        } else if (clientClass == RunStepsClient.class) {
            return clientClass.cast(openAi.runStepsClient());
        } else if (clientClass == VectorStoresClient.class) {
            return clientClass.cast(openAi.vectorStoresClient());
        } else if (clientClass == VectorStoreFilesClient.class) {
            return clientClass.cast(openAi.vectorStoreFilesClient());
        } else if (clientClass == VectorStoreFileBatchesClient.class) {
            return clientClass.cast(openAi.vectorStoreFileBatchesClient());
        } else {
            throw new IllegalArgumentException("Unsupported client type: " + clientClass.getName());
        }
    }
}
