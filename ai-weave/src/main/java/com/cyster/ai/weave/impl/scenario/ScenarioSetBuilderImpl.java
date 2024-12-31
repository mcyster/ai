package com.cyster.ai.weave.impl.scenario;

import java.util.ArrayList;
import java.util.List;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.cyster.ai.weave.service.scenario.ScenarioSet;
import com.cyster.ai.weave.service.scenario.ScenarioSetBuilder;

public class ScenarioSetBuilderImpl<CONTEXT> implements ScenarioSetBuilder<CONTEXT> {
    private List<Scenario<?, CONTEXT>> scenarios = new ArrayList<>();;

    @Override
    public ScenarioSetBuilder<CONTEXT> addScenario(Scenario<?, CONTEXT> scenario) {
        scenarios.add(scenario);
        return this;
    }

    @Override
    public ScenarioSetBuilder<CONTEXT> addScenarios(List<Scenario<?, CONTEXT>> scenarios) {
        for (var scenario : scenarios) {
            this.scenarios.add(scenario);
        }
        return this;
    }

    @Override
    public ScenarioSetBuilder<CONTEXT> addScenarioLoader(ScenarioLoader<CONTEXT> scenarioLoader) {
        for (var scenario : scenarioLoader.getScenarios()) {
            this.scenarios.add(scenario);
        }
        return this;
    }

    @Override
    public ScenarioSetBuilder<CONTEXT> addScenarioLoaders(List<ScenarioLoader<CONTEXT>> scenarioLoaders) {
        for (var loader : scenarioLoaders) {
            addScenarioLoader(loader);
        }

        return this;
    }

    @Override
    public ScenarioSet<CONTEXT> create() {
        return new ScenarioSetImpl<CONTEXT>(this.scenarios);
    }

}
