package com.extole.sage.scenarios.support;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.cyster.sherpa.service.scenario.Scenario;
import com.extole.sage.advisors.runbooks.ExtoleTicketRunbookExecutingAdvisor;
import com.fasterxml.jackson.annotation.JsonProperty;
import  com.extole.sage.scenarios.support.ExtoleTicketRunbookExecutingScenario.Parameters;

@Component
public class ExtoleTicketRunbookExecutingScenario implements Scenario<Parameters, Void> {
    public static String NAME = "extole_ticket_runbook_executor";

    private Advisor<Void> advisor;

    ExtoleTicketRunbookExecutingScenario(ExtoleTicketRunbookExecutingAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Find the best Runbook for an Extole ticket and execute it";
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
        return this.advisor.createConversation().start();
    }

    public static class Parameters {
        @JsonProperty(required = true)
        public String ticket_number;
    }
    

}
