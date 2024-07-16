package com.extole.weave.scenarios.help;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.AssistantScenarioBuilder;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.weave.session.ExtoleSessionContext;

@Component
public class ExtoleHelpScenario implements Scenario<Void, ExtoleSessionContext> {
    public static String NAME = "extoleHelp";
    private final String DESCRIPTION = "Helps using the Extole Platform";

    private AiWeaveService aiWeaveService;
    private Optional<Scenario<Void, ExtoleSessionContext>> scenario = Optional.empty();

    ExtoleHelpScenario(AiWeaveService aiWeaveService) {
        this.aiWeaveService = aiWeaveService;
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
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }


    @Override
    public Conversation createConversation(Void parameters, ExtoleSessionContext context) {
        throw new UnsupportedOperationException("Method is deprectated and being removed from interface");
    }

    @Override
    public ConversationBuilder createConversationBuilder(Void parameters, ExtoleSessionContext context) {
        return this.getScenario().createConversationBuilder(parameters, context);
    }
    
    private Scenario<Void, ExtoleSessionContext> getScenario() {

        if (this.scenario.isEmpty()) {
            String instructions = """
You help with questions around using the Extole SaaS Marketing platform.
""";

            AssistantScenarioBuilder<Void, ExtoleSessionContext> builder = this.aiWeaveService.getOrCreateAssistantScenario(NAME);

            builder
                .setInstructions(instructions)
                .withTool(new ExtoleMeTool())
                .withTool(new ExtoleClientTool())
                .withTool(new ExtoleMyAuthorizationsTool())
                .withTool(new ExtoleClientTimelineTool());

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this;
    }
}
