package com.cyster.weave.store.service;

import java.util.List;
import java.util.Optional;

import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.scenario.ScenarioType;

public interface ScenarioConversationStore {
    Optional<ScenarioConversation> getConversation(String id);

    <PARAMETERS, CONTEXT> ScenarioConversation addConversation(ActiveConversation scenarioConversation,
            ScenarioType scenarioType, PARAMETERS parameers, CONTEXT context);

    QueryBuilder createQueryBuilder();

    public interface QueryBuilder {

        QueryBuilder withFilterParameter(String name, String value);

        QueryBuilder setOffset(int offset);

        QueryBuilder setLimit(int limit);

        List<ScenarioConversation> list();
    }
}
