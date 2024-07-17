package com.extole.weave.scenarios.runbooks;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;

@Component
class ExtoleTicketRunbookTool implements Tool<RunbookScenarioParameters, Void> {
    private ExtoleSupportTicketRunbookScenario extoleTicketRunbookScenario;
    Map<String, RunbookScenario> runbookScenarios;

    ExtoleTicketRunbookTool(ExtoleSupportTicketRunbookScenario extoleTicketRunbookScenario, List<RunbookScenario> runbookScenarios) {
        this.extoleTicketRunbookScenario = extoleTicketRunbookScenario;
        this.runbookScenarios = runbookScenarios.stream()
            .collect(Collectors.toMap(RunbookScenario::getName, runbook -> runbook));
    }

    @Override
    public String getName() {
        return "extoleTicketRunbook";
    }

    @Override
    public String getDescription() {
        return "Finds the best runbook and executes it against the ticket";
    }

    @Override
    public Class<RunbookScenarioParameters> getParameterClass() {
        return RunbookScenarioParameters.class;
    }

    @Override
    public Object execute(RunbookScenarioParameters request, Void context) throws ToolException {
        // TODO support adding conversation as sub-context on run info message

        var runbookName = findRunbook(request);
        
        System.out.println("!!! findRunbook: " + runbookName);

        return executeRunbook(request, runbookName);
    }
    
    
    private String findRunbook(RunbookScenarioParameters request) throws ToolException {
        Conversation conversation = extoleTicketRunbookScenario.createConversationBuilder(request, null)
            .addMessage("Ticket Number: " + request.getTicketNumber())
            .start();

        Message message;
        try {
            message = conversation.respond();
        } catch (ConversationException exception) {
           throw new ToolException("findRunbook failed to start conversation", exception);
        }

        System.out.println("!!! findRunbook convo: " + message);
        
        Optional<String> json = extractJson(message.getContent());
        if (json.isEmpty()) {
            throw new ToolException("findRunbook responce not json");
        }
        ObjectMapper objectMapper = new ObjectMapper();


        JsonNode result;
        try {
            result = objectMapper.readTree(json.get());
        } catch (JsonProcessingException exception) {
            throw new ToolException("findRunbook did not recieve valid json", exception);
        }

        String ticketNumber = result.path("ticket_number").asText();
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new ToolException("findRunbook with no ticket. json: " + json.get());
        }

        String runbookName = result.path("runbook").asText();
        if (!runbookScenarios.containsKey(runbookName)) {
            throw new ToolException("findRunbook unknown runbook. json:" + json.get());
        }
        
        return runbookName;
    }

    private String executeRunbook(RunbookScenarioParameters request, String runbookName) throws ToolException {
        var ticketNumber = request.getTicketNumber();
        var scenario = runbookScenarios.get(runbookName);
        var parameters = new RunbookScenarioParameters(ticketNumber);
        Conversation conversation = scenario.createConversationBuilder(parameters, null).start();

        Message message;
        try {
            conversation.addMessage(Type.USER, ticketNumber);
            message = conversation.respond();
        } catch (ConversationException e) {
            throw new ToolException("rubookScenarion[" + runbookName + "] failed to start conversation");
        }

        System.out.println("!!! executedRunbook: " + message.getContent());

        return message.getContent();
    }
    
    public static Optional<String> extractJson(String input) {
        int braceCounter = 0;
        int startIndex = -1;
        int endIndex = -1;

        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '{') {
                if (startIndex == -1) {
                    startIndex = i;
                }
                braceCounter++;
            } else if (input.charAt(i) == '}') {
                braceCounter--;
                if (braceCounter == 0 && startIndex != -1) {
                    endIndex = i;
                    break;
                }
            }
        }

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return Optional.of(input.substring(startIndex, endIndex + 1));
        } else {
            return Optional.empty();
        }
    }


}
