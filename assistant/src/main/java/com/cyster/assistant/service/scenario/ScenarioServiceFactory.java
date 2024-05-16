package com.cyster.assistant.service.scenario;

import java.util.List;

public interface ScenarioServiceFactory {
    ScenarioService createScenarioService(List<ScenarioLoader> loader, List<Scenario<?,?>> scenarios);
}
