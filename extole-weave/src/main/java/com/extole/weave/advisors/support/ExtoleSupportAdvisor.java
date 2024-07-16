package com.extole.weave.advisors.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.AdvisorService;

@Component
public class ExtoleSupportAdvisor implements Advisor<Void> {
    public final String NAME = "extoleSupport";

    private AdvisorService advisorService;
    private Map<String, ExtoleSupportTool<?>> tools = new HashMap<>();
    private Optional<Advisor<Void>> advisor = Optional.empty();

    public ExtoleSupportAdvisor(AdvisorService advisorService, List<ExtoleSupportAdvisorToolLoader> toolLoaders, List<ExtoleSupportTool<?>> tools) {
        this.advisorService = advisorService;

        for(var tool: tools) {
            this.tools.put(tool.getName(), tool);
        }

        for(var loader: toolLoaders) {
            for(var tool: loader.getTools()) {
                this.tools.put(tool.getName(), tool);
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public AdvisorConversationBuilder<Void> createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """
You are an advisor the support team at Extole a SaaS marketing platform.

Keep answers brief, and where possible in point form.
When referring to a client, use the client short_name.
""";

            AdvisorBuilder<Void> builder = this.advisorService.getOrCreateAdvisor(NAME);
            builder
                .setInstructions(instructions);

           for(var tool: tools.values()) {
                builder.withTool(tool);
           }

            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}
