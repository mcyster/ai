package com.cyster.web.weave.scenarios;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.web.weave.scenarios.ManagedWebsites.ManagedWebsite;
import com.cyster.web.weave.scenarios.WebsiteDeveloperScenario.Request;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class WebsiteDeveloperScenario implements Scenario<Request, ManagedWebsites> {
    private static final String DESCRIPTION = "Build a website";
    private static final String INSTRUCTIONS = """
            You are a sklled web page developer.

            Use the associated website tools, by listing and getting files to understand what the website does.
            The web page will serve the file index.html, so you should read the context of that file with the webdeveloper_file_get tool

            Unless explicitly asked, do not show the user source code, just update or create files as needed.

            If you modify the page leave the script tag for /sites/managed/chat/chat.js in place, this implements a chat window to talk to you on the page.

            If the user asks to add or remove tags this is managed with <meta name="tags" content="">, where the content is a comma seperated list of tags.

            Tell the user the Url of the web page.
            Then ask the user how they would like to modify the website.
            Use the web_developer_file_put tool to create or update the website as requested by the user.
            """;

    private final Advisor<ManagedWebsites> advisor;

    public WebsiteDeveloperScenario(AiAdvisorService aiAdvisorService, List<WebsiteDeveloperTool<?>> tools) {
        AdvisorBuilder<ManagedWebsites> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(INSTRUCTIONS);
        for (var tool : tools) {
            builder.withTool(tool);
        }

        this.advisor = builder.getOrCreate();
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
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Class<ManagedWebsites> getContextClass() {
        return ManagedWebsites.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Request parameters, ManagedWebsites context) {
        String messageTemplate = "There is a website with id %s at %s (we're in developer mode, so localhost is ok)";

        ManagedWebsite website;
        try {
            website = context.getSite(parameters.websiteId());
        } catch (WebsiteException exception) {
            throw new RuntimeException("Unable to load website: " + parameters.websiteId, exception);
        }

        String message = String.format(messageTemplate, website.site().getId(), website.site().getUri());

        return this.advisor.createConversationBuilder(context).addMessage(message);
    }

    public static record Request(@JsonProperty(required = true) String websiteId) {
    }
}
