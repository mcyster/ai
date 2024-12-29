package com.cyster.weave.session.impl.scenariosessionstore;

import java.util.List;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.cyster.ai.weave.service.scenario.ScenarioType;
import com.cyster.weave.session.service.scenariosession.ScenarioConversation;

public class ScenarioConversationImpl implements ScenarioConversation {
    private ActiveConversation activeConversation;
    private ScenarioType scenarioType;
    private Object parameters;
    private Object context;

    public ScenarioConversationImpl(ActiveConversation activeConversation, ScenarioType scenarioType, Object parameters,
            Object context) {
        this.activeConversation = activeConversation;
        this.scenarioType = scenarioType;
        this.parameters = parameters;
        this.context = context;
    }

    @Override
    public Message addMessage(Type type, String message) {
        return this.activeConversation.addMessage(type, message);
    }

    @Override
    public Message respond() throws ConversationException {
        return this.activeConversation.respond();
    }

    @Override
    public Message respond(Weave weave) throws ConversationException {
        return this.activeConversation.respond(weave);
    }

    @Override
    public List<Message> messages() {
        return this.activeConversation.messages();
    }

    @Override
    public String id() {
        return this.activeConversation.id();
    }

    @Override
    public ScenarioType scenarioType() {
        return this.scenarioType;
    }

    @Override
    public Object parameters() {
        return this.parameters;
    }

    @Override
    public Object context() {
        return this.context;
    }

}
