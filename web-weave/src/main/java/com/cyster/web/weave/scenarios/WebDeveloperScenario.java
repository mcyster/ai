package com.cyster.web.weave.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.web.weave.scenarios.WebDeveloperScenario.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class WebDeveloperScenario implements Scenario<Parameters, ManagedWebsites> {
    private static final String DESCRIPTION = "Build a website";

    private WebsiteDeveloperScenario websiteDeveloperScenario;
    private ManagedWebsites managedWebsites;

    WebDeveloperScenario(AiService aiWeaveService, WebsiteProvider websiteProvider,
            WebsiteDeveloperScenario builderScenario) {
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
    public Class<ManagedWebsites> getContextClass() {
        return ManagedWebsites.class;
    }

    @Override
    public ActiveConversationBuilder<ManagedWebsites> createConversationBuilder(Parameters parameters,
            ManagedWebsites context) {
        var request = new WebsiteDeveloperScenario.Request(parameters.websiteId());
        return websiteDeveloperScenario.createConversationBuilder(request, managedWebsites);
    }

    public static record Parameters(@JsonProperty(required = true) String websiteId) {
    }
}