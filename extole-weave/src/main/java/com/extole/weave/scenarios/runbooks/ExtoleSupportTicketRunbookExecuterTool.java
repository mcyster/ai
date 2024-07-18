package com.extole.weave.scenarios.runbooks;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;

@Component
public class ExtoleSupportTicketRunbookExecuterTool implements Tool<RunbookScenarioParameters, Void> {
    private AiWeaveService aiWeaveService;
    Map<String, RunbookScenario> runbookScenarios;

    ExtoleSupportTicketRunbookExecuterTool(AiWeaveService aiWeaveService, 
        ExtoleRunbookScenarioLoader runbookScenarioLoader,
        List<RunbookScenario> runbookScenarios) {
        this.aiWeaveService = aiWeaveService;
        
        this.runbookScenarios = runbookScenarios.stream()
            .collect(Collectors.toMap(RunbookScenario::getName, runbook -> runbook));

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
    public Object execute(RunbookScenarioParameters request, Void context) throws ToolException {
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
        
        Conversation conversation = scenario.createConversationBuilder(request, null)
            .addMessage(requestJson)
            .start();

        Message message;
        try {
            message = conversation.respond();
        } catch (ConversationException exception) {
           throw new ToolException("execute runbook failed to start conversation", exception);
        }
        
        var response = new Response(request.ticketNumber(), request.runbookName(), request.clientId(), request.clientShortName(), message.getContent());
        return response;
    }

    public record Response (
        @JsonProperty(required = true) String ticketNumber,
        @JsonProperty(required = true) String runbookName,
        @JsonProperty(required = true) String clientId,
        @JsonProperty(required = true) String clientShortName,
        @JsonProperty(required = true) String message        
    ) {}
}
