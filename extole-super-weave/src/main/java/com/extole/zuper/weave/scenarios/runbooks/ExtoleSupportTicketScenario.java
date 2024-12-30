package com.extole.zuper.weave.scenarios.runbooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiScenarioService;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioBuilder;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.VoidToolAdapter;
import com.cyster.scheduler.impl.SchedulerTool;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.client.ExtoleSupportTicketClientTool;
import com.extole.zuper.weave.scenarios.runbooks.ExtoleSupportTicketScenario.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleSupportTicketScenario implements Scenario<Parameters, ExtoleSuperContext> {
    private final String DESCRIPTION = "Execute the best Runbook for the specified ticket";

    private AiScenarioService aiScenarioService;
    private Optional<Scenario<Parameters, ExtoleSuperContext>> scenario = Optional.empty();
    private List<Tool<?, ExtoleSuperContext>> tools = new ArrayList<>();

    public ExtoleSupportTicketScenario(AiScenarioService aiScenarioService,
            ExtoleSupportTicketRunbookSelectorTool runbookSelectorTool, ExtoleSupportTicketClientTool ticketClientTool,
            ExtoleSupportTicketRunbookExecuterTool runbookExecuterTool, SchedulerTool schedulerTool) {
        this.aiScenarioService = aiScenarioService;
        this.tools.add(runbookSelectorTool);
        this.tools.add(ticketClientTool);
        this.tools.add(runbookExecuterTool);
        this.tools.add(new VoidToolAdapter<>(schedulerTool, ExtoleSuperContext.class));
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
    public ActiveConversationBuilder<ExtoleSuperContext> createConversationBuilder(Parameters parameters,
            ExtoleSuperContext context) {
        return this.getScenario().createConversationBuilder(parameters, context)
                .addMessage("Ticket: " + parameters.ticketNumber());
    }

    private Scenario<Parameters, ExtoleSuperContext> getScenario() {
        if (this.scenario.isEmpty()) {
            String defaultInstruction = """
                    For the given ticket
                    - find the best runbook.
                    - execute the runbook.

                    Respond with the ticket_number followed by a selected runbook in brackets and then a brief summary of your analysis, i.e:
                    TICKET_NUMBER (RUNBOOK): SUMMARY
                    """;
            ScenarioBuilder<Parameters, ExtoleSuperContext> builder = this.aiScenarioService
                    .getOrCreateScenario(getName());

            builder.setInstructions(defaultInstruction);
            for (var tool : tools) {
                builder.withTool(tool);
            }

            this.scenario = Optional.of(builder.getOrCreate());
        }
        return this.scenario.get();
    }

    public record Parameters(@JsonProperty(required = true) String ticketNumber) {
    }

}
