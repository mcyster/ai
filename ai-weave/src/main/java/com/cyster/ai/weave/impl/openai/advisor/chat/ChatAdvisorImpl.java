package com.cyster.ai.weave.impl.openai.advisor.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cyster.ai.weave.impl.MessageImpl;
import com.cyster.ai.weave.impl.WeaveOperation;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.conversation.Message;

import io.github.stefanbratanov.jvm.openai.OpenAI;

public class ChatAdvisorImpl<C> implements Advisor<C> {

    private OpenAI openAi;
    private String name;
    private List<Message> messages;

    ChatAdvisorImpl(OpenAI openAi, String name, List<Message> messages) {
        this.openAi = openAi;
        this.name = name;
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

    public class ConversationBuilder implements ActiveConversationBuilder<C> {
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
        public ActiveConversationBuilder<C> addMessage(String message) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ActiveConversation start() {
            // TODO implement overrideInstruction
            return new ChatAdvisorConversation(ChatAdvisorImpl.this.openAi, ChatAdvisorImpl.this.messages);
        }
    }

    static class Builder<C2> {
        private OpenAI openAi;
        private String name;
        private List<Message> messages;

        Builder(OpenAI openAi) {
            this.openAi = openAi;
            this.messages = new ArrayList<Message>();
        }

        public Builder<C2> setName(String name) {
            this.name = name;
            return this;
        }

        public Builder<C2> addUserMessage(String content, WeaveOperation operation) {
            this.messages.add(new MessageImpl(Message.Type.USER, content, operation));
            return this;
        }

        public Builder<C2> addSystemMessage(String content, WeaveOperation operation) {
            this.messages.add(new MessageImpl(Message.Type.SYSTEM, content, operation));
            return this;
        }

        public Builder<C2> addAiMessage(String content, WeaveOperation operation) {
            this.messages.add(new MessageImpl(Message.Type.AI, content, operation));
            return this;
        }

        public ChatAdvisorImpl<C2> create() {
            return new ChatAdvisorImpl<C2>(openAi, name, this.messages);
        }
    }
}
