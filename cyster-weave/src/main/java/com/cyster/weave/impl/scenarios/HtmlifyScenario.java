package com.cyster.weave.impl.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.weave.impl.advisors.SimpleAdvisor;

@Component
public class HtmlifyScenario implements Scenario<Void, Void> {
    private static final String NAME = "htmlify";
        
    private Advisor<Void> advisor;

    HtmlifyScenario(SimpleAdvisor advisor) {
        this.advisor = advisor;
    }
    
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Marks up text with html";
    }
    
    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }
    
    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Void parameters, Void context) {
        String instructions = """
Convert the input to a HTML marked up fragment.

Everything should be included in <p></p> tags. If there are lists use <ul> or <ol> tags.

Just returned the marked up fragment, nothing else.  
""";

        return this.advisor.createConversation()
            .setOverrideInstructions(instructions)
            .start();
    }
            
}
