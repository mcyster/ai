package com.extole.weave.scenarios.runbooks;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.extole.weave.advisors.support.ExtoleSupportAdvisor;

@Component
public class ExtoleRunbookOther implements RunbookScenario {
    public static String NAME = "extoleRunbookOther";
    private static String DESCRIPTION = "Analyzes and comments on tickets that could not be classfied more specifically";
    private static String KEYWORDS = "nothing";

    private static String INSTRUCTIONS = """
Load the support ticket {{ticket_number}}

Note the ticket number, and note its classified as "other".
""";


    private Advisor<Void> advisor;

    ExtoleRunbookOther(ExtoleSupportAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getKeywords() {
        return KEYWORDS;
    }

    @Override
    public Class<RunbookScenarioParameters> getParameterClass() {
        return RunbookScenarioParameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(RunbookScenarioParameters parameters, Void context) {
        return this.advisor.createConversation().setOverrideInstructions(INSTRUCTIONS).start();
    }
}

