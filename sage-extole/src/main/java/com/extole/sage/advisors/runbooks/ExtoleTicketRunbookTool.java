package com.extole.sage.advisors.runbooks;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.impl.advisor.Tool;
import com.cyster.sherpa.impl.advisor.ToolException;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.conversation.ConversationException;
import com.cyster.sherpa.service.conversation.Message;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.extole.sage.advisors.runbooks.ExtoleTicketRunbookTool.Request;
import com.extole.sage.scenarios.runbooks.RunbookScenario;
import com.extole.sage.scenarios.runbooks.RunbookScenarioParameters;

@Component
class ExtoleTicketRunbookTool implements Tool<Request, Void> {
    private ExtoleTicketRunbookSelectingAdvisor extoleTicketRunbookSelectingAdvisor;
    Map<String, RunbookScenario> runbookScenarios;
    
    ExtoleTicketRunbookTool(ExtoleTicketRunbookSelectingAdvisor extoleTicketRunbookAdvisor, List<RunbookScenario> runbookScenarios) {
        this.extoleTicketRunbookSelectingAdvisor = extoleTicketRunbookAdvisor;
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
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        Conversation conversation = extoleTicketRunbookSelectingAdvisor.createConversation().start();
        
        conversation.addMessage(request.ticket_number);
        
        // TODO support adding conversation as sub-context on run info message
        
        Message message;
        try {
            message = conversation.respond();
        } catch (ConversationException exception) {
           throw new ToolException("extoleTicketRunbookSelectingAdvisor failed to start conversation", exception);
        }
        
        System.out.println("! extoleTicketRunbookSelectingAdvisor " + message.getContent());
        
        Optional<String> json = extractJson(message.getContent());
        if (json.isEmpty()) {
            throw new ToolException("extoleTicketRunbookSelectingAdvisor did not respond with json");
        }
        ObjectMapper objectMapper = new ObjectMapper();
        
        
        JsonNode result;
        try {
            result = objectMapper.readTree(json.get());
        } catch (JsonProcessingException exception) {
            throw new ToolException("extoleTicketRunbookSelectingAdvisor did not respond with invalid json", exception);
        }
        
        String ticketNumber = result.path("ticket_number").asText();
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new ToolException("extoleTicketRunbookSelectingAdvisor responded with no ticket. json: " + json.get());        
        }
        
        String runbookName = result.path("runbook").asText();
        if (!runbookScenarios.containsKey(runbookName)) {
            throw new ToolException("extoleTicketRunbookSelectingAdvisor responded with unknown runbook. json:" + json.get());    
        }
        
        var scenario = runbookScenarios.get(runbookName);
        var parameters = new RunbookScenarioParameters(ticketNumber);
        Conversation conversation2 = scenario.createConversation(parameters, null);
        
        conversation2.addMessage(ticketNumber);

        Message message2;
        try {
            message2 = conversation2.respond();
        } catch (ConversationException e) {
            throw new ToolException("rubookScenarion[" + runbookName + "] failed to start conversation");
        }
        
        return message2.getContent();
    }

    static class Request {
        @JsonProperty(required = false)
        public String ticket_number;
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
