
package com.extole.sage.scenarios.support;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.sage.advisors.runbooks.ExtoleTicketRunbookSelectingAdvisor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import  com.extole.sage.scenarios.support.ExtoleTicketRunbookScenario.Parameters;

@Component
public class ExtoleTicketRunbookScenario implements Scenario<Parameters, Void> {
    public static String NAME = "extoleTicketRunbook";

    private Advisor<Void> advisor;

    ExtoleTicketRunbookScenario(ExtoleTicketRunbookSelectingAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Find the best Runbook for an Extole ticket";
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
        
        String prompt = "ticket: " + parameters.getTicketNumber();
            
        return this.advisor.createConversation().addMessage(prompt).start();
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


