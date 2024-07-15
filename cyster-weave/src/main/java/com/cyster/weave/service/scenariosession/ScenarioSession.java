package com.cyster.weave.service.scenariosession;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;

public interface ScenarioSession<PARAMETERS, CONTEXT> {

    String getId();
    Scenario<PARAMETERS,CONTEXT> getScenario();
    PARAMETERS getParameters();

    Conversation getConversation();

}
