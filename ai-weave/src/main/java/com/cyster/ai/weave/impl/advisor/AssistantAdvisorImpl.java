package com.cyster.ai.weave.impl.advisor;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.Tool;
import com.cyster.ai.weave.service.conversation.Conversation;

import io.github.stefanbratanov.jvm.openai.Assistant;
import io.github.stefanbratanov.jvm.openai.AssistantsClient;
import io.github.stefanbratanov.jvm.openai.CreateAssistantRequest;
import io.github.stefanbratanov.jvm.openai.File;
import io.github.stefanbratanov.jvm.openai.FilesClient;
import io.github.stefanbratanov.jvm.openai.PaginationQueryParameters;
import io.github.stefanbratanov.jvm.openai.UploadFileRequest;
import io.github.stefanbratanov.jvm.openai.AssistantsClient.PaginatedAssistants;



public class AssistantAdvisorImpl<C> implements Advisor<C> {
    public static String VERSION = "0.1";
    public static String METADATA_VERSION = "version";
    public static String METADATA_IDENTITY = "identityHash";

    private OpenAiService openAiService;
    private Assistant assistant;
    private Toolset<C> toolset;

    public AssistantAdvisorImpl(OpenAiService openAiService, Assistant assistant, Toolset<C> toolset) {
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
    public ConversationBuilder<C> createConversation() {
        return new ConversationBuilder<C>(this);
    }

    public static class ConversationBuilder<C2> implements Advisor.ConversationBuilder<C2> {
        private Optional<String> overrideInstructions = Optional.empty();
        private C2 context = null;
        private AssistantAdvisorImpl<C2> advisor;
        private List<String> messages = new ArrayList<String>();

        private ConversationBuilder(AssistantAdvisorImpl<C2> advisor) {
            this.advisor = advisor;
        }

        @Override
        public ConversationBuilder<C2> withContext(C2 context) {
            this.context = context;
            return this;
        }

        @Override
        public ConversationBuilder<C2> setOverrideInstructions(String instructions) {
            this.overrideInstructions = Optional.of(instructions);
            return this;
        }

        @Override
        public ConversationBuilder<C2> addMessage(String message) {
            this.messages.add(message);
            return this;
        }

        @Override
        public Conversation start() {
            var conversation = new AssistantAdvisorConversation<C2>(this.advisor.openAiService,
                this.advisor.assistant.id(),
                this.advisor.toolset,
                overrideInstructions,
                context);

            //for (var message : this.messages) {
            //    conversation.addMessage(message);
            //}

            return conversation;
        }
    }

    public static class Builder<C2> implements AdvisorBuilder<C2> {
        private static final String MODEL = "gpt-4o";

        private final OpenAiService openAiService;
        private final String name;
        private Optional<String> instructions = Optional.empty();
        private Toolset.Builder<C2> toolsetBuilder = new Toolset.Builder<C2>();
        private List<Path> filePaths = new ArrayList<Path>();
        
        Builder(OpenAiService openAiService, String name) {
            this.openAiService = openAiService;
            this.name = name;
        }

        @Override
        public Builder<C2> setInstructions(String instructions) {
            this.instructions = Optional.of(instructions);
            return this;
        }

        @Override
        public <T> AdvisorBuilder<C2> withTool(Tool<T, C2> tool) {
            this.toolsetBuilder.addTool(tool);
            return this;
        }

        @Override
        public AdvisorBuilder<C2> withFile(Path path) {
            this.filePaths.add(path);
            return this;
        }

        @Override
        public Advisor<C2> getOrCreate() {
            String hash = this.getHash();

            var assistant = this.findAssistant(hash);
            if (assistant.isEmpty()) {
                assistant = Optional.of(this.create(hash));
            }

            return new AssistantAdvisorImpl<C2>(this.openAiService, assistant.get(), this.toolsetBuilder.create());
        }

        private Assistant create(String hash) {
            List<String> fileIds = new ArrayList<String>();
            for (var filePath : this.filePaths) {
                FilesClient filesClient = this.openAiService.createClient(FilesClient.class);
                UploadFileRequest uploadInputFileRequest = UploadFileRequest.newBuilder()
                    .file(filePath)
                    .purpose("assistants")
                    .build();
                File file = filesClient.uploadFile(uploadInputFileRequest);
                fileIds.add(file.id());
            }

            var metadata = new HashMap<String, String>();
            metadata.put(METADATA_VERSION, VERSION);
            metadata.put(METADATA_IDENTITY, hash);

            var toolset = new AdvisorToolset<C2>(this.toolsetBuilder.create());

            AssistantsClient assistantsClient = this.openAiService.createClient(AssistantsClient.class);
            CreateAssistantRequest.Builder requestBuilder = CreateAssistantRequest.newBuilder()
                .name(this.name)
                .model(MODEL)
                .metadata(metadata);
            
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
                PaginationQueryParameters.Builder queryBuilder = PaginationQueryParameters.newBuilder()
                    .limit(99);
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

        private String getHash() {
            String text = VERSION + this.name + this.instructions;
            for (var tool : this.toolsetBuilder.create().getTools()) {
                text = text + tool.getName() + tool.getDescription();
            }

            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            byte[] hashBytes = digest.digest(text.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte value : hashBytes) {
                String hex = Integer.toHexString(0xff & value);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }

    }
}
