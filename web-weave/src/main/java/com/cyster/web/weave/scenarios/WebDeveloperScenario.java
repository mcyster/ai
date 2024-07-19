package com.cyster.web.weave.scenarios;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.web.weave.scenarios.WebsiteDeveloperScenario.Request;
import com.cyster.web.weave.scenarios.WebDeveloperScenario.Parameters;

@Component
public class WebDeveloperScenario implements Scenario<Parameters, Void> {
    private static final String DESCRIPTION = "Build a website";

    private WebsiteDeveloperScenario websiteDeveloperScenario;
    private ManagedWebsites managedWebsites;
    private Optional<Scenario<Request, ManagedWebsites>> scenario = Optional.empty();
    
    WebDeveloperScenario(AiWeaveService aiWeaveService, WebsiteProvider websiteProvider, WebsiteDeveloperScenario builderScenario) {
        this.managedWebsites = new ManagedWebsites(websiteProvider);
        this.websiteDeveloperScenario = builderScenario;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Scenario", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public ConversationBuilder createConversationBuilder(Parameters parameters, Void context) {
       var request = new WebsiteDeveloperScenario.Request(parameters.websiteId());
       return websiteDeveloperScenario.createConversationBuilder(request, managedWebsites);
    }
    
    public static record Parameters(
        @JsonProperty(required = true) String websiteId
    ) {}
}