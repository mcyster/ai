package com.extole.sage.scenarios.support;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.sage.advisors.runbooks.ExtoleTicketRunbookExecutingAdvisor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import  com.extole.sage.scenarios.support.ExtoleSupportTicketScenario.Parameters;

@Component
public class ExtoleSupportTicketScenario implements Scenario<Parameters, Void> {
    public static String NAME = "extoleSupportTicket";

    private Advisor<Void> advisor;

    ExtoleSupportTicketScenario(ExtoleTicketRunbookExecutingAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Analyzes and comment on Extole Support Tickets";
    }
    
    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }
 
    @Override
    public Conversation createConversation(Parameters parameters, Void context) {
        if (parameters == null || parameters.ticketNumber == null || parameters.ticketNumber.isBlank()) {
            throw new IllegalArgumentException("No ticketNumber specified"); // TODO not runtime
        }
                
        return this.advisor.createConversation().addMessage("Ticket: " + parameters.ticketNumber).start();
    }

    public static final class Parameters {
        @JsonProperty(required = true)
        private final String ticketNumber;

        @JsonCreator
        public Parameters(@JsonProperty("ticketNumber") String ticketNumber) {
            this.ticketNumber = ticketNumber;
        }

        public String getTicketNumber() {
            return ticketNumber;
        }
    }
}
