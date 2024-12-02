package com.cyster.ai.weave.impl.advisor.assistant;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.cyster.ai.weave.impl.advisor.Advisor;
import com.cyster.ai.weave.impl.advisor.AdvisorBuilder;
import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolContextFactory;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.Message.Type;

import io.github.stefanbratanov.jvm.openai.Assistant;
import io.github.stefanbratanov.jvm.openai.AssistantsClient;
import io.github.stefanbratanov.jvm.openai.AssistantsClient.PaginatedAssistants;
import io.github.stefanbratanov.jvm.openai.CreateAssistantRequest;
import io.github.stefanbratanov.jvm.openai.File;
import io.github.stefanbratanov.jvm.openai.FilesClient;
import io.github.stefanbratanov.jvm.openai.PaginationQueryParameters;
import io.github.stefanbratanov.jvm.openai.UploadFileRequest;

public class AssistantAdvisorImpl<SCENARIO_CONTEXT> implements Advisor<SCENARIO_CONTEXT> {

    public static String VERSION = "0.1";
    public static String METADATA_VERSION = "version";
    public static String METADATA_IDENTITY = "identityHash";

    private OpenAiService openAiService;
    private Assistant assistant;
    private Toolset toolset;

    public AssistantAdvisorImpl(OpenAiService openAiService, Assistant assistant, Toolset toolset) {
        this.openAiService = openAiService;
        this.assistant = assistant;
        this.toolset = toolset;
    }

    public String getId() {
        return this.assistant.id();
    }

    public String getName() {
        return this.assistant.name();
    }

    @Override
    public ConversationBuilder<SCENARIO_CONTEXT> createConversation() {
        return new ConversationBuilder<SCENARIO_CONTEXT>(this);
    }

    public static class ConversationBuilder<CONTEXT> implements Advisor.AdvisorConversationBuilder<CONTEXT> {
        private Optional<String> overrideInstructions = Optional.empty();
        private CONTEXT context = null;
        private AssistantAdvisorImpl<CONTEXT> advisor;
        private List<String> messages = new ArrayList<String>();

        private ConversationBuilder(AssistantAdvisorImpl<CONTEXT> advisor) {
            this.advisor = advisor;
        }

        @Override
        public ConversationBuilder<CONTEXT> withContext(CONTEXT context) {
            this.context = context;
            return this;
        }

        @Override
        public ConversationBuilder<CONTEXT> setOverrideInstructions(String instructions) {
            this.overrideInstructions = Optional.of(instructions);
            return this;
        }

        @Override
        public ConversationBuilder<CONTEXT> addMessage(String message) {
            this.messages.add(message);
            return this;
        }

        @Override
        public Conversation start() {
            var conversation = new AssistantAdvisorConversation<CONTEXT>(this.advisor.openAiService,
                    this.advisor.getName(), this.advisor.getId(), this.advisor.toolset, overrideInstructions, context);

            for (var message : this.messages) {
                conversation.addMessage(Type.USER, message);
            }

            return conversation;
        }
    }

    public static class Builder<CONTEXT> implements AdvisorBuilder<CONTEXT> {
        private static final String MODEL = "gpt-4o";

        private final OpenAiService openAiService;
        private final String name;
        private final Toolset.Builder toolsetBuilder;

        private final List<Path> filePaths = new ArrayList<Path>();
        private Optional<String> instructions = Optional.empty();

        public Builder(OpenAiService openAiService, ToolContextFactory toolContextFactory, String name) {
            this.openAiService = openAiService;
            this.name = name;
            this.toolsetBuilder = new Toolset.Builder(toolContextFactory);

        }

        @Override
        public Builder<CONTEXT> setInstructions(String instructions) {
            this.instructions = Optional.of(instructions);
            return this;
        }

        @Override
        public <TOOL_PARAMETERS, TOOL_CONTEXT> AdvisorBuilder<CONTEXT> withTool(
                Tool<TOOL_PARAMETERS, TOOL_CONTEXT> tool) {
            this.toolsetBuilder.addTool(tool);
            return this;
        }

        @Override
        public AdvisorBuilder<CONTEXT> withFile(Path path) {
            this.filePaths.add(path);
            return this;
        }

        @Override
        public Advisor<CONTEXT> getOrCreate() {
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
            metadata.put(METADATA_VERSION, VERSION);
            metadata.put(METADATA_IDENTITY, hash);

            var toolset = new AdvisorToolset(this.toolsetBuilder.create());

            AssistantsClient assistantsClient = this.openAiService.createClient(AssistantsClient.class);
            CreateAssistantRequest.Builder requestBuilder = CreateAssistantRequest.newBuilder().name(this.name)
                    .model(MODEL).metadata(metadata);

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
                        if (assistant.metadata().containsKey(METADATA_IDENTITY)) {
                            if (assistant.metadata().get(METADATA_IDENTITY).equals(hash)) {
                                return Optional.of(assistant);
                            }
                        }
                    }
                }
            } while (response.hasMore());

            return Optional.empty();
        }

        private int assistantHash() {
            return Objects.hash(MODEL, VERSION, name, instructions, filePaths, this.toolsetBuilder.create().getTools());
        }
    }
}
