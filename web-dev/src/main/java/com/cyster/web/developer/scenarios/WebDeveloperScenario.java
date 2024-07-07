package com.cyster.web.developer.scenarios;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.web.developer.advisors.WebAdvisor;
import com.cyster.web.developer.advisors.WebsiteService;
import com.cyster.web.developer.advisors.WebsiteService.Website;
import com.cyster.web.developer.scenarios.WebDeveloperScenario.Parameters;

@Component
public class WebDeveloperScenario implements Scenario<Parameters, Void> {
    private static final String NAME = "webDeveloper";
    private WebAdvisor advisor;
    private WebsiteService websiteService;

    WebDeveloperScenario(WebAdvisor advisor, WebsiteService websiteService) {
        this.advisor = advisor;
        this.websiteService = websiteService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Build a website";
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
    public Conversation createConversation(Parameters parameters, Void context) {
        
        Website website;
        if (parameters != null && parameters.siteName() != null && !parameters.siteName().isBlank()) {
            website = this.websiteService.getSite(parameters.siteName());
        } 
        else {
            String indexHtml = loadAsset("/web/simple/index.html");
           
            website = this.websiteService.create();
            website
                .putAsset("index.html", indexHtml);
        }
        
        String instructions = """ 
There is a web page at %s 
- we're in developer mode, so localhost is ok 

Use the associated website tools, by listing and getting files to understand what the website does. 
The web page will serve the file index.html, so you should read the context of that file with the webdeveloper_file_get tool 

Unless explicitly asked, do not show the user source code, just update or create files as needed. 

Tell the user the Url of the web page.  
Then ask the user how they would like to modify the website. 
Use the web_developer_file_put tool to create or update the website as requested by the user.
""";
        
        return advisor.createConversation()
            .withContext(website)
            .setOverrideInstructions(String.format(instructions, website.getUri().toString()))
            .start();
    }
    
    public static String loadAsset(String assetPath) {
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
