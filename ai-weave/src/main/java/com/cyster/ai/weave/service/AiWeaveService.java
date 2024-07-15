package com.cyster.ai.weave.service;


// TBD should project / this just be an AiScenarioService

public interface AiWeaveService {

    <PARAMETERS, CONTEXT> AssistantScenarioBuilder<PARAMETERS, CONTEXT> getOrCreateAssistantScenario(String name);
    
}
