package com.extole.zuper.weave.scenarios.prehandler;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
public class ExtoleJavascriptPrehandlerActionScenario implements Scenario<Void, Void> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleJavascriptPrehandlerActionScenario.class);

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Void, Void>> scenario = Optional.empty();
    private ExtoleApiStore extoleStore;

    ExtoleJavascriptPrehandlerActionScenario(AiWeaveService aiWeaveService, ExtoleApiStore extoleStore) {
        this.aiWeaveService = aiWeaveService;
        this.extoleStore = extoleStore;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Scenario", "");
    }

    @Override
    public String getDescription() {
        return "Helps with writing and debugging prehandlers";
    }

    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public com.cyster.ai.weave.service.scenario.Scenario.ConversationBuilder createConversationBuilder(Void parameters,
            Void context) {
        return this.getScenario().createConversationBuilder(parameters, context);
    }

    private Scenario<Void, Void> getScenario() {
        if (this.scenario.isEmpty()) {

            String resourcePath = "/extole/scenario/prehandler_action_context.js";
            URL resourceUrl = ExtoleJavascriptPrehandlerActionScenario.class.getResource(resourcePath);
            if (resourceUrl == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            Path javascriptActionContextPath;
            try {
                javascriptActionContextPath = Paths.get(resourceUrl.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Unable to convert resourceUrl to URI");
            }
            logger.debug("javascriptActionContextPath at: " + javascriptActionContextPath);

            String instructions = """
                    The JavaScript code described here executes with a variable 'context' of type PrehandlerActionContext.
                    It should create a processedRawEvent using the ProcessedRawEventBuilder available from the context.

                    To understand how to use the 'context' you need explore the api for classes like:
                       - PrehandlerActionContext
                       - PrehandlerContext
                       - GlobalContext
                       - LoggerContext
                       - ClientContext
                       - GlobalServices
                       - ProcessedRawEventBuilder
                            """;
            instructions = """
                    A prehandler can modify a raw event before its processed by Extole.
                    The request is modified using the ProcessedRawEventBuilder available as getEventBuilder from the context variable.

                    A prehandler code snippet prehandler_javascript_code is executed in the following context.

                    var context = PrehandlerActionContext(javaPrehandlerContext);
                    (function(context) {
                      // ... prehandler_javascript_code here ...
                    })(context)

                    To understand how to use the 'context' you need explore the api for classes like:
                     - PrehandlerActionContext
                     - PrehandlerContext
                     - GlobalContext
                     - LoggerContext
                     - ClientContext
                     - GlobalServices
                     - ProcessedRawEventBuilder

                     Where possible, link to interfaces and classes mentioned in your response.
                    """;

            AssistantScenarioBuilder<Void, V> builder = this.aiWeaveService.getOrCreateAssistantScenario(getName());

            builder.setInstructions(instructions);

            // TODO update to use SearchTool
            // .withFile(javascriptActionContextPath);

            builder.withTool(extoleStore.createStoreTool());

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }

}
