package com.cyster.ai.weave.impl.advisor.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cyster.ai.weave.impl.openai.OpenAiService;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;

public class AssistantAdvisorConversation<CONTEXT> implements Conversation {

    private List<Message> messages = new ArrayList<>();
    private List<Message> newMessages = new ArrayList<>();
    private AssistantAdvisorThread<CONTEXT> assistantAdvisorThread;

    AssistantAdvisorConversation(OpenAiService openAiService, String assistantName, String assistantId, Toolset toolset,
            Optional<String> overrideInstructions, CONTEXT context) {
        this.assistantAdvisorThread = new AssistantAdvisorThread<>(openAiService, assistantName, assistantId, toolset,
                overrideInstructions, context);
    }

    @Override
    public Message addMessage(Type type, String content) {
        var message = new MessageImpl(type, content);

        newMessages.add(message);

        return message;
    }

    @Override
    public Message respond() throws ConversationException {
        var operation = new OperationImpl("Assistant", new ArrayList<>(newMessages));

        messages.addAll(newMessages);
        var response = this.assistantAdvisorThread.respond(this.newMessages, operation);
        newMessages.clear();

        messages.add(response);

        return response;
    }

    @Override
    public Message respond(OperationLogger operation) throws ConversationException {
        var childOperation = operation.childLogger("Assistant", new ArrayList<>(newMessages));

        messages.addAll(newMessages);
        var response = this.assistantAdvisorThread.respond(this.newMessages, childOperation);
        newMessages.clear();
        messages.add(response);

        return response;
    }

    @Override
    public List<Message> getMessages() {
        return Stream.concat(this.messages.stream(), this.newMessages.stream()).collect(Collectors.toList());
    }

}
