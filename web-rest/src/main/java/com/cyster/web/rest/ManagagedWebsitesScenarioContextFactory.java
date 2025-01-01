package com.cyster.web.rest;

import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.cyster.weave.rest.conversation.ScenarioContextFactory;
import com.cyster.web.weave.scenarios.ManagedWebsites;
import com.cyster.web.weave.scenarios.WebsiteProvider;

@Component
public class ManagagedWebsitesScenarioContextFactory implements ScenarioContextFactory<ManagedWebsites> {
    private final WebsiteProvider websiteProvider;

    ManagagedWebsitesScenarioContextFactory(WebsiteProvider websiteProvider) {
        this.websiteProvider = websiteProvider;
    }

    @Override
    public Class<ManagedWebsites> getContextClass() {
        return ManagedWebsites.class;
    }

    @Override
    public ManagedWebsites createContext(MultiValueMap<String, String> headers) {
        return new ManagedWebsites(websiteProvider);
    }
}
