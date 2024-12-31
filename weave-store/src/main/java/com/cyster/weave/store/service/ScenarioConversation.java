package com.cyster.weave.store.service;

import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.scenario.ScenarioType;

public interface ScenarioConversation extends ActiveConversation {

    ScenarioType scenarioType();

    Object parameters();

    Object context();
}
