package com.cyster.ai.weave.impl.advisor.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;

public class AssistantAdvisorConversation<CONTEXT> implements ActiveConversation, Weave {
    private final String id;
    private final List<Message> messages = new ArrayList<>();
    private final List<Message> newMessages = new ArrayList<>();
    private final AssistantAdvisorThread<CONTEXT> assistantAdvisorThread;
    private final WeaveOperation operation;

    AssistantAdvisorConversation(OpenAiService openAiService, String assistantName, String assistantId, Toolset toolset,
            Optional<String> overrideInstructions, CONTEXT context) {
        this.id = UUID.randomUUID().toString();
        this.assistantAdvisorThread = new AssistantAdvisorThread<>(openAiService, assistantName, assistantId, toolset,
                overrideInstructions, context);

        this.operation = new OperationImpl("Assistant Conversation", new ArrayList<>(newMessages));
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Message addMessage(Type type, String content) {
        var message = new MessageImpl(type, content,
                this.operation().childLogger("add message(" + type + "): " + content));

        newMessages.add(message);

        return message;
    }

    @Override
    public Message respond() throws ConversationException {
        messages.addAll(newMessages);
        var response = this.assistantAdvisorThread.respond(this.newMessages, this);
        newMessages.clear();

        messages.add(response);

        return response;
    }

    @Override
    public Message respond(Weave weave) throws ConversationException {
        var childWeave = new WeaveImpl(weave.conversation(),
                weave.operation().childLogger("Assistant", new ArrayList<>(newMessages)));

        messages.addAll(newMessages);
        var response = this.assistantAdvisorThread.respond(this.newMessages, childWeave);
        newMessages.clear();
        messages.add(response);

        return response;
    }

    @Override
    public List<Message> messages() {
        return Stream.concat(this.messages.stream(), this.newMessages.stream()).collect(Collectors.toList());
    }

    @Override
    public ActiveConversation conversation() {
        return this;
    }

    @Override
    public WeaveOperation operation() {
        return operation;
    }

}
