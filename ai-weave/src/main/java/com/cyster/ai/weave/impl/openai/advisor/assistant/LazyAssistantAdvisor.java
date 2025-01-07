package com.cyster.ai.weave.impl.openai.advisor.assistant;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.impl.openai.advisor.assistant.code.CodeInterpreterToolImpl;
import com.cyster.ai.weave.impl.openai.advisor.assistant.store.SearchToolImpl;
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
import io.github.stefanbratanov.jvm.openai.Tool.FileSearchTool.FileSearch;
import io.github.stefanbratanov.jvm.openai.ToolResources;
import io.github.stefanbratanov.jvm.openai.UploadFileRequest;

public class LazyAssistantAdvisor<CONTEXT> implements Advisor<CONTEXT> {

    private final OpenAiService openAiService;
    private final String name;
    private final Toolset.Builder<CONTEXT> toolsetBuilder;

    private final List<Path> filePaths;
    private final Optional<SearchToolImpl<CONTEXT>> searchTool;
    private final Optional<CodeInterpreterToolImpl<CONTEXT>> codeInterpreterTool;
    private final Optional<String> instructions;

    private AtomicReference<AssistantAdvisorImpl<CONTEXT>> advisor = new AtomicReference<>();

    LazyAssistantAdvisor(OpenAiService openAiService, String name, Toolset.Builder<CONTEXT> toolsetBuilder,
            Optional<SearchToolImpl<CONTEXT>> searchTool,
            Optional<CodeInterpreterToolImpl<CONTEXT>> codeInterpreterTool, List<Path> filePaths,
            Optional<String> instructions) {
        this.openAiService = openAiService;
        this.name = name;
        this.toolsetBuilder = toolsetBuilder;
        this.filePaths = filePaths;
        this.searchTool = searchTool;
        this.codeInterpreterTool = codeInterpreterTool;

        this.instructions = instructions;

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(CONTEXT context) {
        return getAdvisor().createConversationBuilder(context);
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

        var toolset = new AssistantAdvisorToolset<CONTEXT>(this.toolsetBuilder.create());

        AssistantsClient assistantsClient = this.openAiService.createClient(AssistantsClient.class);
        CreateAssistantRequest.Builder requestBuilder = CreateAssistantRequest.newBuilder().name(this.name)
                .model(AssistantAdvisorImpl.MODEL).metadata(metadata);

        if (codeInterpreterTool.isPresent()) {
            requestBuilder.tool(new io.github.stefanbratanov.jvm.openai.Tool.CodeInterpreterTool());
            fileIds.addAll(codeInterpreterTool.get().getFileIds());
        }

        List<String> vectorStoreIds = null;
        if (searchTool.isPresent()) {
            var search = new FileSearch(Optional.of(10), Optional.empty());
            requestBuilder.tool(new io.github.stefanbratanov.jvm.openai.Tool.FileSearchTool(Optional.of(search)));
            vectorStoreIds = List.of(searchTool.get().getVectorStore().id());
        }

        if (vectorStoreIds != null && fileIds != null) {
            var resources = ToolResources.codeInterpreterAndFileSearchToolResources(fileIds,
                    vectorStoreIds.toArray(new String[0]));
            requestBuilder.toolResources(resources);
        } else if (fileIds != null) {
            var resources = ToolResources.codeInterpreterToolResources(fileIds);
            requestBuilder.toolResources(resources);
        } else if (vectorStoreIds != null) {
            var resources = ToolResources.fileSearchToolResources(vectorStoreIds.toArray(new String[0]));
            requestBuilder.toolResources(resources);
        }

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
