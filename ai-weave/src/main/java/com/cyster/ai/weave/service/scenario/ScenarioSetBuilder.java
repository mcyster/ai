package com.cyster.ai.weave.service.scenario;

import java.util.List;

public interface ScenarioSetBuilder<CONTEXT> {

    ScenarioSetBuilder<CONTEXT> addScenario(Scenario<?, CONTEXT> scenario);

    ScenarioSetBuilder<CONTEXT> addScenarios(List<Scenario<?, CONTEXT>> scenario);

    ScenarioSetBuilder<CONTEXT> addScenarioLoader(ScenarioLoader<CONTEXT> scenarioLoader);

    ScenarioSetBuilder<CONTEXT> addScenarioLoaders(List<ScenarioLoader<CONTEXT>> scenarioLoaders);

    ScenarioSet<CONTEXT> create();

}
