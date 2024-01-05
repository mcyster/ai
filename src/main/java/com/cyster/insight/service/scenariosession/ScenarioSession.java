package com.cyster.insight.service.scenariosession;

import com.cyster.sage.service.conversation.Conversation;
import com.cyster.sage.service.scenario.Scenario;

public interface ScenarioSession {

	public String getId();

	public Scenario getScenario();
	
	public Conversation getConversation();
	
}
