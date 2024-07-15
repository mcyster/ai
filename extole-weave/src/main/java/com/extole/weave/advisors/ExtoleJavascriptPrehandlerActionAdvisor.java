package com.extole.weave.advisors;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.extole.weave.advisors.ExtoleJavascriptPrehandlerActionAdvisor.AdminUserToolContext;

@Component
public class ExtoleJavascriptPrehandlerActionAdvisor implements Advisor<AdminUserToolContext> {
    public final String NAME = "extolePrehandlerAction";

    private AdvisorService advisorService;
    private Optional<Advisor<AdminUserToolContext>> advisor = Optional.empty();
    private ExtoleStore extoleStore;

    public ExtoleJavascriptPrehandlerActionAdvisor(AdvisorService advisorService, ExtoleStore extoleStore) {
        this.advisorService = advisorService;
        this.extoleStore = extoleStore;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<AdminUserToolContext> createConversation() {
        if (this.advisor.isEmpty()) {

            String resourcePath = "/extole/scenario/prehandler_action_context.js";
            URL resourceUrl = ExtoleJavascriptPrehandlerActionAdvisor.class.getResource(resourcePath);
            if (resourceUrl == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            Path javascriptActionContextPath;
            try {
                javascriptActionContextPath = Paths.get(resourceUrl.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Unable to convert resourceUrl to URI");
            }

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

            AdvisorBuilder<AdminUserToolContext> builder = this.advisorService.getOrCreateAdvisor(NAME);

            builder
                .setInstructions(instructions)
                .withFile(javascriptActionContextPath);

            builder.withTool(extoleStore.createStoreTool());

            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }


    public static class AdminUserToolContext {
        private String userToken;

        public AdminUserToolContext(String userToken) {
            this.userToken = userToken;
        }

        public String getUserToken() {
            return this.userToken;
        }
    }

}
