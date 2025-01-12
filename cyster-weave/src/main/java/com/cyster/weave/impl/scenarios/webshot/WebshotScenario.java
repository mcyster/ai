package com.cyster.weave.impl.scenarios.webshot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.template.StringTemplate;
import com.cyster.weave.impl.scenarios.webshot.WebshotScenario.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
@ConditionalOnBean(WebshotTool.class)
public class WebshotScenario implements Scenario<Request, Void> {
    private final String DESCRIPTION = "Turns the specified url into an image";

    private final Advisor<Void> advisor;
    private LocalAssetProvider localAssetProvider;

    public WebshotScenario(AiAdvisorService aiAdvisorService, WebshotTool webshotTool,
            LocalAssetProvider localAssetProvider) {

        var instructions = """
                You convert webpages into images
                """;

        AdvisorBuilder<Void> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(instructions).withTool(webshotTool).withTool(webshotTool);

        builder.withTool(webshotTool);

        this.advisor = builder.getOrCreate();

        this.localAssetProvider = localAssetProvider;
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
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Request parameters, Void context) {
        var instructionsTemplate = """
                Take a snapshot of the page at the url {{url}} and provide a link to the resultant image at
                """;

        var instructions = new StringTemplate(instructionsTemplate).render(parameters);

        return this.advisor.createConversationBuilder(context).setOverrideInstructions(instructions);
    }

    static record Request(
            @JsonPropertyDescription("Url to web page convert to an image") @JsonProperty(required = true) String url) {
    }
}
