package com.cyster.ai.weave.impl.openai.advisor.assistant;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.impl.tool.Toolset;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.ToolContextFactory;

import io.github.stefanbratanov.jvm.openai.Assistant;

public class AssistantAdvisorImpl<SCENARIO_CONTEXT> implements Advisor<SCENARIO_CONTEXT> {
    public static final String MODEL = "gpt-4o";
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
    public ConversationBuilder<SCENARIO_CONTEXT> createConversationBuilder(SCENARIO_CONTEXT context) {
        return new ConversationBuilder<SCENARIO_CONTEXT>(this, context);
    }

    public static class ConversationBuilder<CONTEXT> implements ActiveConversationBuilder<CONTEXT> {
        private Optional<String> overrideInstructions = Optional.empty();
        private CONTEXT context;
        private AssistantAdvisorImpl<CONTEXT> advisor;
        private List<String> messages = new ArrayList<String>();

        private ConversationBuilder(AssistantAdvisorImpl<CONTEXT> advisor, CONTEXT context) {
            this.advisor = advisor;
            this.context = context;
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
        public ActiveConversation start() {
            var conversation = new AssistantAdvisorConversation<CONTEXT>(this.advisor.openAiService,
                    this.advisor.getName(), this.advisor.getId(), this.advisor.toolset, overrideInstructions, context);

            for (var message : this.messages) {
                conversation.addMessage(Type.USER, message);
            }

            return conversation;
        }
    }

    public static class Builder<CONTEXT> implements AdvisorBuilder<CONTEXT> {

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
        public AdvisorBuilder<CONTEXT> withTool(Tool<?, CONTEXT> tool) {
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
            var advisor = new LazyAssistantAdvisor<CONTEXT>(openAiService, name, toolsetBuilder, filePaths,
                    instructions);

            return advisor;
        }
    }
}
