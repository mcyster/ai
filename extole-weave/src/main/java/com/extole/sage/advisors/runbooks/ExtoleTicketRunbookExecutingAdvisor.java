package com.extole.sage.advisors.runbooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.advisor.Tool;


@Component
public class ExtoleTicketRunbookExecutingAdvisor implements Advisor<Void> {
    public final String NAME = "extoleTicketRunbookExector";

    private AdvisorService advisorService;
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private Optional<Advisor<Void>> advisor = Optional.empty();

    public ExtoleTicketRunbookExecutingAdvisor(AdvisorService advisorService, ExtoleTicketRunbookTool runbookTool) {
        this.advisorService = advisorService;
        this.tools.add(runbookTool);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<Void> createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """ 
Given the ticket execute the Runbook. 
Respond with the ticket_number followed by a colon then a summary of your analysis, i.e: 
TICKET_NUMBER: SUMMARY
""";

            AdvisorBuilder<Void> builder = this.advisorService.getOrCreateAdvisor(NAME);
            builder
                .setInstructions(instructions);
                
           for(var tool: tools) {
                builder.withTool(tool);
           }

            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}

