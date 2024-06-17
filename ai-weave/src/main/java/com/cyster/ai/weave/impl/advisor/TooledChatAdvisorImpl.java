package com.cyster.ai.weave.impl.advisor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.Tool;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.Message;

import io.github.stefanbratanov.jvm.openai.OpenAI;

public class TooledChatAdvisorImpl<C> implements Advisor<C> {

    private OpenAI openAi;
    private String name;
    private Toolset<C> toolset;
    private List<Message> messages;

    TooledChatAdvisorImpl(OpenAI openAi, String name, Toolset<C> toolset, List<Message> messages) {
        this.openAi = openAi;
        this.name = name;
        this.toolset = toolset;
        this.messages = messages;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ConversationBuilder createConversation() {
        return new ConversationBuilder();
    }

    public class ConversationBuilder implements Advisor.ConversationBuilder<C> {
        Optional<String> overrideInstructions = Optional.empty();
        C context = null;

        private ConversationBuilder() {
        }

        @Override
        public ConversationBuilder withContext(C context) {
            this.context = context;
            return this;
        }

        public ConversationBuilder setOverrideInstructions(String instructions) {
            this.overrideInstructions = Optional.of(instructions);
            return this;
        }

        @Override
        public com.cyster.ai.weave.service.advisor.Advisor.ConversationBuilder<C> addMessage(String message) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Conversation start() {
            // TODO implement overrideInstruction
            return new TooledChatAdvisorConversation<C>(TooledChatAdvisorImpl.this.openAi, 
                TooledChatAdvisorImpl.this.toolset, TooledChatAdvisorImpl.this.messages);
        }
    }

    static class Builder<C2> {
        private OpenAI openAi;
        private String name;
        private List<Message> messages;
        private Toolset.Builder<C2> toolsetBuilder;

        Builder(OpenAI openAiService) {
            this.openAi = openAiService;
            this.messages = new ArrayList<Message>();
            this.toolsetBuilder = new Toolset.Builder<C2>();
        }

        public Builder<C2> setName(String name) {
            this.name = name;
            return this;
        }

        public Builder<C2> addUserMessage(String content) {
            this.messages.add(new MessageImpl(content));
            return this;
        }

        public Builder<C2> addSystemMessage(String content) {
            this.messages.add(new MessageImpl(Message.Type.SYSTEM, content));
            return this;
        }

        public Builder<C2> addAiMessage(String content) {
            this.messages.add(new MessageImpl(Message.Type.AI, content));
            return this;
        }

        public <T> Builder<C2> addTool(Tool<?, C2> tool) {
            this.toolsetBuilder.addTool(tool);
            return this;
        }

        public TooledChatAdvisorImpl<C2> create() {
            return new TooledChatAdvisorImpl<C2>(openAi, name, this.toolsetBuilder.create(), this.messages);
        }
    }
}
