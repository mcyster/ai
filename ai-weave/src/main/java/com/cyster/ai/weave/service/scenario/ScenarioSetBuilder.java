package com.cyster.ai.weave.service.scenario;

import java.util.List;

public interface ScenarioSetBuilder {

    ScenarioSetBuilder addScenario(Scenario<?,?> scenario);
    ScenarioSetBuilder addScenarios(List<Scenario<?,?>> scenario);
    ScenarioSetBuilder addScenarioLoader(ScenarioLoader scenarioLoader);
    ScenarioSetBuilder addScenarioLoaders(List<ScenarioLoader> scenarioLoaders);
    
    ScenarioSet create();
        
}
