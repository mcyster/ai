package com.cyster.weave.session.impl.scenariosessionstore;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.weave.session.service.scenariosession.ScenarioSession;

public class ScenarioSessionImpl<PARAMETERS, CONTEXT> implements ScenarioSession<PARAMETERS, CONTEXT> {
    private String id;
    private Scenario<PARAMETERS,CONTEXT> scenario;
    private PARAMETERS parameters;
    private Conversation conversation;

    ScenarioSessionImpl(String id, Scenario<PARAMETERS,CONTEXT> scenario, PARAMETERS parameters, Conversation conversation) {
        this.id = id;
        this.scenario = scenario;
        this.parameters = parameters;
        this.conversation = conversation;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Scenario<PARAMETERS,CONTEXT> getScenario() {
        return this.scenario;
    }

    @Override
    public PARAMETERS getParameters() {
        return this.parameters;
    }

    @Override
    public Conversation getConversation() {
        return this.conversation;
    }


}
