package com.cyster.weave.session.service.scenariosession;

import java.util.List;
import java.util.Optional;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;

public interface ScenarioSessionStore {
    Optional<ScenarioSession<?, ?>> getSession(String id);

    <PARAMETERS, CONTEXT> ScenarioSession<PARAMETERS, CONTEXT> addSession(String id,
            Scenario<PARAMETERS, CONTEXT> scenario, PARAMETERS parameters, Conversation conversation);

    QueryBuilder createQueryBuilder();

    public interface QueryBuilder {

        QueryBuilder withFilterParameter(String name, String value);

        QueryBuilder setOffset(int offset);

        QueryBuilder setLimit(int limit);

        List<ScenarioSession<?, ?>> list();
    }
}
