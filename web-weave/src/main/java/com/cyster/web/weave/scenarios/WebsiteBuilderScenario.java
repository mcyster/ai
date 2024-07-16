package com.cyster.web.weave.scenarios;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.web.weave.scenarios.WebsiteService.Website;

@Component
public class WebsiteBuilderScenario implements Scenario<Void, Website> {
    public static final String NAME = "websiteBuilder";
    private static final String DESCRIPTION = "Build a website";
    private static final String INSTRUCTIONS = """
You are a sklled web page developer.

Use the associated website tools, by listing and getting files to understand what the website does.
The web page will serve the file index.html, so you should read the context of that file with the webdeveloper_file_get tool

Unless explicitly asked, do not show the user source code, just update or create files as needed.

Tell the user the Url of the web page.
Then ask the user how they would like to modify the website.
Use the web_developer_file_put tool to create or update the website as requested by the user.
""";
    
    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Void, Website>> scenario = Optional.empty();    
    private Map<String, Tool<?, Website>> tools = new HashMap<>();

    public WebsiteBuilderScenario(AiWeaveService aiWeaveService,
        WebsiteFileListTool websiteFileListTool,
        WebsiteFileGetTool websiteFileGetTool,
        WebsiteFilePutTool websiteFilePutTool) {
        this.aiWeaveService = aiWeaveService;

        this.tools.put(websiteFileListTool.getName(), websiteFileListTool);
        this.tools.put(websiteFileGetTool.getName(), websiteFileGetTool);
        this.tools.put(websiteFilePutTool.getName(), websiteFilePutTool);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Class<Void> getParameterClass() {
       return Void.class;
    }

    @Override
    public Class<Website> getContextClass() {
        return Website.class;
    }
    
    @Override
    public Conversation createConversation(Void parameters, Website context) {
        throw new UnsupportedOperationException("Method is deprectated and being removed from interface");
    }
    
    @Override
    public ConversationBuilder createConversationBuilder(Void parameters, Website context) {        
        String message = "There is a web page at %s (we're in developer mode, so localhost is ok)";
        
        return this.getScenario().createConversationBuilder(parameters, context)
            .addMessage(String.format(message, context.getUri()));
    }
    
    private Scenario<Void, Website> getScenario() {
        if (this.scenario.isEmpty()) {
            AssistantScenarioBuilder<Void, Website> builder = this.aiWeaveService.getOrCreateAssistantScenario(NAME);
            
            builder.setInstructions(INSTRUCTIONS);
            for(var tool: tools.values()) {
                builder.withTool(tool);
            }

            this.scenario = Optional.of(builder.getOrCreate());
        }
        
        return this.scenario.get();
    }
    
}
