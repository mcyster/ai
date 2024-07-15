package com.extole.sage.scenarios.help;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.sage.advisors.client.ExtoleClientAdvisor;
import com.extole.sage.session.ExtoleSessionContext;

@Component
public class ExtoleHelpScenario implements Scenario<Void, ExtoleSessionContext> {
    public static String NAME = "extoleHelp";
    
    private ExtoleClientAdvisor advisor;

    ExtoleHelpScenario(ExtoleClientAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Helps using the Extole Platform";
    }
    
    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }
    

    @Override
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }


    @Override
    public Conversation createConversation(Void parameters, ExtoleSessionContext context) {
        var advisorContext = new ExtoleClientAdvisor.Context(context.getAccessToken());
        
        return advisor.createConversation().withContext(advisorContext).start();
    }
}
