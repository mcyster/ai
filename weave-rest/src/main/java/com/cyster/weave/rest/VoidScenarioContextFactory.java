package com.cyster.weave.rest;

import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.cyster.weave.rest.conversation.ScenarioContextFactory;

@Component
public class VoidScenarioContextFactory implements ScenarioContextFactory<Void> {

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Void createContext(String conversationId, MultiValueMap<String, String> headers) {
        return null;
    }

}
