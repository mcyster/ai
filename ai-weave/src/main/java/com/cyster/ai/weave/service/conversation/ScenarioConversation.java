package com.cyster.ai.weave.service.conversation;

import com.cyster.ai.weave.service.scenario.ScenarioType;

public interface ScenarioConversation extends AdvisorConversation {

    ScenarioType scenarioType();

    Object parameters();

    Object context();
}
