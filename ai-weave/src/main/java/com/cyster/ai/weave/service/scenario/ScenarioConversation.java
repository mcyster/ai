package com.cyster.ai.weave.service.scenario;

import com.cyster.ai.weave.service.conversation.ActiveConversation;

public interface ScenarioConversation extends ActiveConversation {

    ScenarioType scenarioType();

    Object parameters();

    Object context();
}
