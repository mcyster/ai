package com.extole.zuper.weave.scenarios.runbooks;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.ToolException;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ExtoleSupportTicketRunbookExecuterTool implements Tool<RunbookScenarioParameters, ExtoleSuperContext> {
    Map<String, RunbookSuperScenario> runbookScenarios;

    ExtoleSupportTicketRunbookExecuterTool(AiService aiWeaveService,
            ExtoleRunbookScenarioLoader runbookScenarioLoader, List<RunbookSuperScenario> runbookScenarios) {
        this.runbookScenarios = runbookScenarios.stream()
                .collect(Collectors.toMap(RunbookSuperScenario::getName, runbook -> runbook));

        for (var scenario : runbookScenarioLoader.getRunbookScenarios()) {
            this.runbookScenarios.put(scenario.getName(), scenario);
        }
    }

    @Override
    public String getName() {
        return "extoleTicketRunbookExecutor";
    }

    @Override
    public String getDescription() {
        return "Run the specified runbook against the ticket";
    }

    @Override
    public Class<RunbookScenarioParameters> getParameterClass() {
        return RunbookScenarioParameters.class;
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public Object execute(RunbookScenarioParameters request, ExtoleSuperContext context, Weave weave)
            throws ToolException {
        var scenario = this.runbookScenarios.get(request.runbookName());
        if (scenario == null) {
            throw new ToolException("Runbook " + request.runbookName() + " not found");
        }

        String requestJson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            requestJson = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to convert to json: " + request.toString());
        }

        ActiveConversation conversation = scenario.createConversationBuilder(request, context).addMessage(requestJson)
                .start();

        Message message;
        try {
            message = conversation.respond(weave);
        } catch (ConversationException exception) {
            throw new ToolException("execute runbook failed to start conversation", exception);
        }

        var response = new Response(request.ticketNumber(), request.runbookName(), request.clientId(),
                request.clientShortName(), message.getContent());
        return response;
    }

    public int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass(), runbookScenarios);
    }

    public record Response(@JsonProperty(required = true) String ticketNumber,
            @JsonProperty(required = true) String runbookName, @JsonProperty(required = true) String clientId,
            @JsonProperty(required = true) String clientShortName, @JsonProperty(required = true) String message) {
    }

}
