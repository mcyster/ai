package com.cyster.weave.session.service.scenariosession;

import java.util.List;
import java.util.Optional;

import com.cyster.ai.weave.service.scenario.ScenarioConversation;

public interface ScenarioConversationStore {
    Optional<ScenarioConversation> getSession(String id);

    <REQUEST, CONTEXT> ScenarioConversation addConversation(ScenarioConversation scenarioConversation);

    QueryBuilder createQueryBuilder();

    public interface QueryBuilder {

        QueryBuilder withFilterParameter(String name, String value);

        QueryBuilder setOffset(int offset);

        QueryBuilder setLimit(int limit);

        List<ScenarioConversation> list();
    }
}
