package com.extole.admin.weave.scenarios.prehandler;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.admin.weave.scenarios.prehandler.ExtoleJavascriptPrehandlerActionScenario.Parameters;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleJavascriptPrehandlerActionScenario implements Scenario<Parameters, ExtoleSessionContext> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleJavascriptPrehandlerActionScenario.class);

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Parameters, ExtoleSessionContext>> scenario = Optional.empty();
    private ExtoleApiStore extoleStore;
    private ExtolePrehandlerGetTool extolePrehandlerGetTool;

    ExtoleJavascriptPrehandlerActionScenario(AiWeaveService aiWeaveService, ExtoleApiStore extoleStore,
            ExtolePrehandlerGetTool extolePrehandlerGetTool) {
        this.aiWeaveService = aiWeaveService;
        this.extoleStore = extoleStore;
        this.extoleStore = extoleStore;
        this.extolePrehandlerGetTool = extolePrehandlerGetTool;
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
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public com.cyster.ai.weave.service.scenario.Scenario.ConversationBuilder createConversationBuilder(
            Parameters parameters, ExtoleSessionContext context) {
        var builder = this.getScenario().createConversationBuilder(parameters, context);

        if (parameters != null && parameters.prehandlerId != null) {
            builder.addMessage("Please load and review the prehandler with perhandlerId: " + parameters.prehandlerId
                    + " to help with context of this discussion");
        }

        return builder;
    }

    private Scenario<Parameters, ExtoleSessionContext> getScenario() {

        if (this.scenario.isEmpty()) {
            String instructions = """
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

            AssistantScenarioBuilder<Parameters, ExtoleSessionContext> builder = this.aiWeaveService
                    .getOrCreateAssistantScenario(getName());

            builder.setInstructions(instructions);
            builder.withTool(extoleStore.createStoreTool());
            builder.withTool(extolePrehandlerGetTool);

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }

    public static record Parameters(@JsonProperty(required = false) String prehandlerId) {
    };
}
