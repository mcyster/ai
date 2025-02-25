package com.extole.zuper.weave.scenarios.support;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.tool.VoidToolAdapter;
import com.cyster.scheduler.impl.SchedulerTool;
import com.cyster.weave.impl.scenarios.conversation.ConversationLinkTool;
import com.cyster.weave.impl.scenarios.webshot.WebshotTool;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportAdvisorToolLoader;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportTool;

@Component
public class ExtoleSupportHelpSuperScenario implements Scenario<Void, ExtoleSuperContext> {
    private static String DESCRIPTION = "Help with the Extole platform for members of the Extole Support Team";

    private final Advisor<ExtoleSuperContext> advisor;

    ExtoleSupportHelpSuperScenario(AiAdvisorService aiAdvisorService, List<ExtoleSupportAdvisorToolLoader> toolLoaders,
            List<ExtoleSupportTool<?>> tools, ConversationLinkTool conversationLinkTool, SchedulerTool schedulerTool,
            WebshotTool webshotTool) {
        String instructions = """
                You are an senior member of the support team at Extole a SaaS marketing platform.

                Keep answers brief, and where possible in point form.
                When referring to a client, use the client short_name.
                """;

        AdvisorBuilder<ExtoleSuperContext> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(instructions);

        for (var tool : tools) {
            builder.withTool(tool);
        }
        for (var loader : toolLoaders) {
            for (var tool : loader.getTools()) {
                builder.withTool(tool);
            }
        }

        builder.withTool(new VoidToolAdapter<>(schedulerTool, ExtoleSuperContext.class));
        builder.withTool(new VoidToolAdapter<>(conversationLinkTool, ExtoleSuperContext.class));
        builder.withTool(new VoidToolAdapter<>(webshotTool, ExtoleSuperContext.class));

        this.advisor = builder.getOrCreate();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("SuperScenario", "");
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
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Void parameters, ExtoleSuperContext context) {
        return this.advisor.createConversationBuilder(context);
    }

}
