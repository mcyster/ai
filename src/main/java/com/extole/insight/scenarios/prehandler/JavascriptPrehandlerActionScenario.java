package com.extole.insight.scenarios.prehandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cyster.insight.service.advisor.Advisor;
import com.cyster.insight.service.conversation.Conversation;
import com.cyster.insight.service.scenario.Scenario;
import com.extole.insight.advisors.ExtoleJavascriptPrehandlerActionAdvisor;

@Component
public class JavascriptPrehandlerActionScenario implements Scenario {
    public static String NAME = "extole_prehandler_action";
    
    private Advisor advisor;
    
    private Map<String, String> defaultVariables = new HashMap<String, String>();

    JavascriptPrehandlerActionScenario(ExtoleJavascriptPrehandlerActionAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Set<String> variables() {
        return defaultVariables.keySet();
    }

    @Override
    public ConversationBuilder createConversation() {
        return new ConversationBuilder(this.advisor);
    }
    
    public class ConversationBuilder implements Scenario.ConversationBuilder {
        private Advisor advisor;
        
        ConversationBuilder(Advisor advisor) {
            this.advisor = advisor;
        }

        @Override
        public ConversationBuilder setContext(Map<String, String> context) {
            return this;
        }

        @Override
        public Conversation start() {
            return this.advisor.createConversation().start();
        }
    }


}
