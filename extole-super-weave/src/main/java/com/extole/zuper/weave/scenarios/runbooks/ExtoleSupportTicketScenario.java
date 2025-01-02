package com.extole.zuper.weave.scenarios.runbooks;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.tool.VoidToolAdapter;
import com.cyster.scheduler.impl.SchedulerTool;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.client.ExtoleSupportTicketClientTool;
import com.extole.zuper.weave.scenarios.runbooks.ExtoleSupportTicketScenario.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleSupportTicketScenario implements Scenario<Parameters, ExtoleSuperContext> {
    private final String DESCRIPTION = "Execute the best Runbook for the specified ticket";

    private final Advisor<ExtoleSuperContext> advisor;

    public ExtoleSupportTicketScenario(AiAdvisorService aiAdvisorService,
            ExtoleSupportTicketRunbookSelectorTool runbookSelectorTool, ExtoleSupportTicketClientTool ticketClientTool,
            ExtoleSupportTicketRunbookExecuterTool runbookExecuterTool, SchedulerTool schedulerTool) {

        String defaultInstruction = """
                For the given ticket
                - find the best runbook.
                - execute the runbook.

                Respond with the ticket_number followed by a selected runbook in brackets and then a brief summary of your analysis, i.e:
                TICKET_NUMBER (RUNBOOK): SUMMARY
                """;
        AdvisorBuilder<ExtoleSuperContext> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(defaultInstruction);

        builder.withTool(runbookSelectorTool);
        builder.withTool(ticketClientTool);
        builder.withTool(runbookExecuterTool);
        builder.withTool(new VoidToolAdapter<>(schedulerTool, ExtoleSuperContext.class));

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
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Parameters parameters, ExtoleSuperContext context) {
        return this.advisor.createConversationBuilder(context).addMessage("Ticket: " + parameters.ticketNumber());
    }

    public record Parameters(@JsonProperty(required = true) String ticketNumber) {
    }
}
