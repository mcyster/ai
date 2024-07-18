package com.cyster.web.weave.scenarios;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.web.weave.scenarios.WebDeveloperScenario.Parameters;
import com.cyster.web.weave.scenarios.WebsiteProvider.Website;

@Component
public class WebDeveloperScenario implements Scenario<Parameters, Void> {
    private static final String DESCRIPTION = "Build a website";

    private WebsiteProvider websiteProvider;
    private WebsiteBuilderScenario builderScenario;

    WebDeveloperScenario(WebsiteProvider websiteService, WebsiteBuilderScenario builderScenario) {
        this.websiteProvider = websiteService;
        this.builderScenario = builderScenario;
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
        Website website;
        if (parameters != null && parameters.siteName() != null && !parameters.siteName().isBlank()) {
            website = this.websiteProvider.getSite(parameters.siteName());
        }
        else {
            String indexHtml = loadAsset("/web/simple/index.html");

            website = this.websiteProvider.create();
            website
                .putAsset("index.html", indexHtml);
        }

        return builderScenario.createConversationBuilder(null, website);
    }
    
    private static String loadAsset(String assetPath) {
        InputStream stream = WebDeveloperScenario.class.getResourceAsStream(assetPath);
        if (stream == null) {
            throw new RuntimeException("Error unable to load resource:/extole/web/graph/simple/index.html");
        }
        byte[] bytes;
        try {
            bytes = stream.readAllBytes();
        } catch (IOException exception) {
            throw new RuntimeException("Error unable to read resource: /extole/web/graph/simple/index.html", exception);
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    public record Parameters(@JsonProperty(required = false) String siteName) {}

}