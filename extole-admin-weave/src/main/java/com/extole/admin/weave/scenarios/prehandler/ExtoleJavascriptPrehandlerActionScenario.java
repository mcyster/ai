package com.extole.admin.weave.scenarios.prehandler;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.tool.SearchTool;
import com.extole.admin.weave.scenarios.prehandler.ExtoleJavascriptPrehandlerActionScenario.Parameters;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleJavascriptPrehandlerActionScenario implements Scenario<Parameters, ExtoleSessionContext> {

    private final Advisor<ExtoleSessionContext> advisor;

    ExtoleJavascriptPrehandlerActionScenario(AiAdvisorService aiAdvisorService, ExtoleApiStore extoleStore,
            ExtolePrehandlerGetTool extolePrehandlerGetTool) {

        String instructions = """
                 A prehandler can modify a request before its processed by Extole.

                 Prehandler Overview
                 A prehandler modifies a request before it is processed by Extole. It’s passed a `context` that describes the request and allows modifications via `ProcessedRawEventBuilder` (obtained by calling `context.getEventBuilder()`).

                 The prehandler is passed a context that holds a description of the request and allows the request
                 to be modified using ProcessedRawEventBuilder return by context.getEventBuilder()

                 **Prehandler Code Execution Context**
                 A prehandler_code_snippet is executed in the following context:
                 ```javascript
                 var context = PrehandlerActionContext(javaPrehandlerContext);
                 (function(context) {
                   // ... prehandler_code_snippet ...
                 })(context)
                 ```
                 Remember, the `(function(content) {}})(context)` wrapper is added by the application, so do not included in your code snippets.

                 To understand how to use the 'context' you need explore the api for classes like:
                  - `PrehandlerActionContext`
                  - `PrehandlerContext`
                  - `GlobalContext`
                  - `LoggerContext`
                  - `ClientContext`
                  - `GlobalServices`
                  - `ProcessedRawEventBuilder`
                 Load these classes using the the search tool.

                The context is readonly, to modify events you must use getEventBuilder() and work with a ProcessedRawEventBuilder.

                 Coding Style
                 - **Readability over performance**: Prioritize clear, easy-to-read code.
                 - **Minimize comments or omit them entirely in code examples:**:, Only include comments for complex code if absolutely necessary.
                 - **Descriptive naming**: Use full words for variables and function names, avoiding abbreviations.
                 - **Defensive coding**: Always check for `null` or other potential issues..
                 - **Check API calls**: Check the code leverages the documented api.
                 """;

        AdvisorBuilder<ExtoleSessionContext> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(instructions);

        SearchTool.Builder<ExtoleSessionContext> searchToolBuilder = builder
                .searchToolBuilder(ExtoleSessionContext.class);
        extoleStore.createStoreTool(searchToolBuilder);

        builder.withTool(extolePrehandlerGetTool);

        this.advisor = builder.getOrCreate();
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
    public ActiveConversationBuilder createConversationBuilder(Parameters parameters, ExtoleSessionContext context) {
        var builder = this.advisor.createConversationBuilder(context);

        if (parameters != null && parameters.prehandlerId != null) {
            builder.addMessage("This discussion is about the prehandler with perhandlerId: " + parameters.prehandlerId
                    + ". Please load the prehandler, for this discussion we are just interested in the actions.jascript attribute.");
        }

        return builder;
    }

    public static record Parameters(@JsonProperty(required = false) String prehandlerId) {
    };
}
