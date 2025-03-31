package com.extole.zuper.weave.scenarios.activity;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.template.StringTemplate;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.activity.ExtoleSupportTicketActivitySuperScenario.Parameters;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketActivitySetTool;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketGetTool;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleSupportTicketActivitySuperScenario implements Scenario<Parameters, ExtoleSuperContext> {
    private final String DESCRIPTION = "Find the best Runbook for the specified ticket";

    private final Advisor<ExtoleSuperContext> advisor;

    public ExtoleSupportTicketActivitySuperScenario(AiService aiService, AiAdvisorService aiAdvisorService,
            ExtoleSupportActivities supportActivities, SupportTicketGetTool ticketGetTool,
            SupportTicketActivitySetTool supportTicketActivitySetTool) {

        List<Activity> activities = supportActivities.loadActivities();

        String instructionsTemplate = """
                        Fetch the specified ticket.
                        If the ticket has no associated activity then classify it using the activity list below and set the activity on the ticket.
                        If you can't determine an activity, leave the activity unset on the ticket.

                        Activities:
                        {{#activities}}
                        - {{name}}
                        {{/activities}}
                """;

        Map<String, Object> parameters = Map.of("activities", activities);

        String instructions = new StringTemplate(instructionsTemplate).render(parameters);

        System.out.println("!!!!!!!! extole support ticket activity instructions: " + instructions);

        AdvisorBuilder<ExtoleSuperContext> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());
        builder.setInstructions(instructions);

        builder.withTool(ticketGetTool);
        builder.withTool(supportTicketActivitySetTool);

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
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Parameters parameters, ExtoleSuperContext context) {
        if (parameters == null || parameters.ticketNumber() == null || parameters.ticketNumber().isBlank()) {
            throw new IllegalArgumentException("No ticketNumber specified");
        }

        return this.advisor.createConversationBuilder(context).addMessage("Ticket: " + parameters.ticketNumber());
    }

    public record Parameters(@JsonProperty(required = true) String ticketNumber) {
    }

    public record Response(@JsonProperty(required = true) String ticketNumber,
            @JsonProperty(required = true) String activityName, @JsonProperty(required = false) String query,
            @JsonProperty(required = false) String[] searchResults) {
    }
}
