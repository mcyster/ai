package com.cyster.weave.store.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.scenario.ScenarioType;
import com.cyster.weave.store.service.ScenarioConversation;
import com.cyster.weave.store.service.ScenarioConversationStore;

@Component
public class SenarioConversationStoreImpl implements ScenarioConversationStore {

    Map<String, ScenarioConversation> store;

    SenarioConversationStoreImpl() {
        this.store = new HashMap<String, ScenarioConversation>();
    }

    public Optional<ScenarioConversation> getSession(String id) {
        if (this.store.containsKey(id)) {
            return Optional.of(this.store.get(id));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <PARAMETERS, CONTEXT> ScenarioConversation addConversation(ActiveConversation activeConversation,
            ScenarioType scenarioType, PARAMETERS parameters, CONTEXT context) {
        if (this.store.containsKey(activeConversation.id())) {
            throw new RuntimeException("Id taken " + activeConversation.id());
        }

        var conversation = new ScenarioConversationImpl(activeConversation, scenarioType, (Object) parameters,
                (Object) context);

        this.store.put(conversation.id(), conversation);

        return conversation;
    }

    public QueryBuilder createQueryBuilder() {
        return new QueryBuilderImpl(this.store);
    }

    public static class QueryBuilderImpl implements ScenarioConversationStore.QueryBuilder {
        Map<String, ScenarioConversation> store;
        int offset = 0;
        int limit = 100;
        Map<String, String> filterParameters = new HashMap<>();

        QueryBuilderImpl(Map<String, ScenarioConversation> store) {
            this.store = store;
        }

        @Override
        public QueryBuilder withFilterParameter(String name, String value) {
            filterParameters.put(name, value);

            return this;
        }

        public QueryBuilder setOffset(int offset) {
            this.offset = offset;
            return this;
        }

        public QueryBuilder setLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public List<ScenarioConversation> list() {
            return this.store.entrySet().stream().filter(this::filterByParameters).skip(this.offset).limit(this.limit)
                    .map(Map.Entry::getValue).collect(Collectors.toList());
        }

        private Boolean filterByParameters(Map.Entry<String, ScenarioConversation> sessionEntry) {
            Boolean match = true;

            Object parameters = sessionEntry.getValue().parameters();
            for (Map.Entry<String, String> filter : filterParameters.entrySet()) {
                try {
                    if (parameters == null) {
                        match = false;
                        break;
                    }

                    Field field = parameters.getClass().getDeclaredField(filter.getKey());
                    field.setAccessible(true);
                    Object fieldValue = field.get(parameters);

                    if (fieldValue == null || !fieldValue.toString().equals(filter.getValue())) {
                        match = false;
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to filter parameters");
                } catch (NoSuchFieldException e) {
                    match = false;
                } catch (SecurityException e) {
                    throw new RuntimeException("Unable to access filter parameter");
                }
            }

            return match;
        }
    }
}
