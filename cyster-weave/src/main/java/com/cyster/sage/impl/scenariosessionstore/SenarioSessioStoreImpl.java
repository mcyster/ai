package com.cyster.sage.impl.scenariosessionstore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.sage.service.scenariosession.ScenarioSession;
import com.cyster.sage.service.scenariosession.ScenarioSessionStore;

@Component
public class SenarioSessioStoreImpl implements ScenarioSessionStore {

    Map<String, ScenarioSession<?,?>> store;

    SenarioSessioStoreImpl() {
        this.store = new HashMap<String, ScenarioSession<?,?>>();
    }

    public Optional<ScenarioSession<?,?>> getSession(String id) {
        if (this.store.containsKey(id)) {
            return Optional.of(this.store.get(id));
        } else {
            return Optional.empty();
        }
    }


    @Override
    public <PARAMETERS, CONTEXT> ScenarioSession<PARAMETERS, CONTEXT> addSession(Scenario<PARAMETERS, CONTEXT> scenario,
        PARAMETERS parameters, Conversation conversation) {
        var id = UUID.randomUUID().toString();
        var session = new ScenarioSessionImpl<PARAMETERS, CONTEXT>(id, scenario, parameters, conversation);

        this.store.put(id, session);

        return session;
    }

    public QueryBuilder createQueryBuilder() {
        return new QueryBuilderImpl(this.store);
    }

    public static class QueryBuilderImpl implements ScenarioSessionStore.QueryBuilder {
        Map<String, ScenarioSession<?,?>> store;
        int offset = 0;
        int limit = 100;
        Map<String, String> filterParameters = new HashMap<>();
        
        QueryBuilderImpl(Map<String, ScenarioSession<?,?>> store) {
            this.store = store;
        }


        @Override
        public QueryBuilder withFilterParameter(String name, String value) {
            filterParameters.put(name,  value);
            
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

        public List<ScenarioSession<?,?>> list() {
            return this.store.entrySet().stream().filter(this::filterByParameters).skip(this.offset).limit(this.limit).map(Map.Entry::getValue)
                .collect(Collectors.toList());
        }
        
        private Boolean filterByParameters(Map.Entry<String,ScenarioSession<?,?>> sessionEntry) {
            Boolean match = true;
            
            Object parameters = sessionEntry.getValue().getParameters();
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
