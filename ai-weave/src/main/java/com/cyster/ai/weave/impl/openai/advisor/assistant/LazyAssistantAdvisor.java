package com.cyster.ai.weave.impl.openai.advisor.assistant;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.impl.tool.Toolset;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;

import io.github.stefanbratanov.jvm.openai.Assistant;
import io.github.stefanbratanov.jvm.openai.AssistantsClient;
import io.github.stefanbratanov.jvm.openai.AssistantsClient.PaginatedAssistants;
import io.github.stefanbratanov.jvm.openai.CreateAssistantRequest;
import io.github.stefanbratanov.jvm.openai.File;
import io.github.stefanbratanov.jvm.openai.FilesClient;
import io.github.stefanbratanov.jvm.openai.PaginationQueryParameters;
import io.github.stefanbratanov.jvm.openai.UploadFileRequest;

public class LazyAssistantAdvisor<CONTEXT> implements Advisor<CONTEXT> {

    private final OpenAiService openAiService;
    private final String name;
    private final Toolset.Builder toolsetBuilder;

    private final List<Path> filePaths;
    private final Optional<String> instructions;

    private AtomicReference<AssistantAdvisorImpl<CONTEXT>> advisor = new AtomicReference<>();

    LazyAssistantAdvisor(OpenAiService openAiService, String name, Toolset.Builder toolsetBuilder, List<Path> filePaths,
            Optional<String> instructions) {
        this.openAiService = openAiService;
        this.name = name;
        this.toolsetBuilder = toolsetBuilder;
        this.filePaths = filePaths;
        this.instructions = instructions;

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ActiveConversationBuilder<CONTEXT> createConversation(CONTEXT context) {
        return getAdvisor().createConversation(context);
    }

    private AssistantAdvisorImpl<CONTEXT> getAdvisor() {
        return advisor.updateAndGet(existing -> {
            if (existing == null) {
                return getOrCreateAdvisor();
            }
            return existing;
        });
    }

    private AssistantAdvisorImpl<CONTEXT> getOrCreateAdvisor() {
        String hash = String.valueOf(assistantHash());

        var assistant = this.findAssistant(hash);
        if (assistant.isEmpty()) {
            assistant = Optional.of(this.create(hash));
        }

        return new AssistantAdvisorImpl<CONTEXT>(this.openAiService, assistant.get(), this.toolsetBuilder.create());
    }

    private Assistant create(String hash) {
        List<String> fileIds = new ArrayList<String>();
        for (var filePath : this.filePaths) {
            FilesClient filesClient = this.openAiService.createClient(FilesClient.class);
            UploadFileRequest uploadInputFileRequest = UploadFileRequest.newBuilder().file(filePath)
                    .purpose("assistants").build();
            File file = filesClient.uploadFile(uploadInputFileRequest);
            fileIds.add(file.id());
        }

        var metadata = new HashMap<String, String>();
        metadata.put(AssistantAdvisorImpl.METADATA_VERSION, AssistantAdvisorImpl.VERSION);
        metadata.put(AssistantAdvisorImpl.METADATA_IDENTITY, hash);

        var toolset = new AssistantAdvisorToolset(this.toolsetBuilder.create());

        AssistantsClient assistantsClient = this.openAiService.createClient(AssistantsClient.class);
        CreateAssistantRequest.Builder requestBuilder = CreateAssistantRequest.newBuilder().name(this.name)
                .model(AssistantAdvisorImpl.MODEL).metadata(metadata);

        toolset.applyTools(requestBuilder);

        if (this.instructions.isPresent()) {
            requestBuilder.instructions(this.instructions.get());
        }

        Assistant assistant = assistantsClient.createAssistant(requestBuilder.build());

        return assistant;
    }

    private Optional<Assistant> findAssistant(String hash) {
        AssistantsClient assistantsClient = this.openAiService.createClient(AssistantsClient.class);

        PaginatedAssistants response = null;
        do {
            PaginationQueryParameters.Builder queryBuilder = PaginationQueryParameters.newBuilder().limit(99);
            if (response != null) {
                queryBuilder.after(response.lastId());
            }
            response = assistantsClient.listAssistants(queryBuilder.build());

            for (var assistant : response.data()) {
                if (assistant.name() != null && assistant.name().equals(this.name)) {
                    if (assistant.metadata().containsKey(AssistantAdvisorImpl.METADATA_IDENTITY)) {
                        if (assistant.metadata().get(AssistantAdvisorImpl.METADATA_IDENTITY).equals(hash)) {
                            return Optional.of(assistant);
                        }
                    }
                }
            }
        } while (response.hasMore());

        return Optional.empty();
    }

    private int assistantHash() {
        return Objects.hash(AssistantAdvisorImpl.MODEL, AssistantAdvisorImpl.VERSION, name, instructions, filePaths,
                this.toolsetBuilder.create().getTools());
    }
}
