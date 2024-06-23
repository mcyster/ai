package com.cyster.sage.impl.scenarios;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.sage.impl.advisors.web.WebAdvisor;
import com.cyster.sage.impl.advisors.web.WebsiteService;
import com.cyster.sage.impl.advisors.web.WebsiteService.Website;
import com.cyster.sage.impl.scenarios.WebsiteScenario.Parameters;

@Component
public class WebsiteScenario implements Scenario<Parameters, Void> {
    private static final String NAME = "websiteBuilder";
    private WebAdvisor advisor;
    private WebsiteService websiteService;

    WebsiteScenario(WebAdvisor advisor, WebsiteService websiteService) {
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
        if (parameters != null && parameters.siteId() != null && !parameters.siteId().isBlank()) {
            website = this.websiteService.getSite(parameters.siteId());
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
        InputStream stream = WebsiteScenario.class.getResourceAsStream(assetPath);
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
    
    public record Parameters(@JsonProperty(required = false) String siteId) {}

}
