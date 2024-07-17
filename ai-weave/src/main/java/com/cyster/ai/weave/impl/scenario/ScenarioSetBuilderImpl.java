package com.cyster.ai.weave.impl.scenario;

import java.util.ArrayList;
import java.util.List;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.cyster.ai.weave.service.scenario.ScenarioSet;
import com.cyster.ai.weave.service.scenario.ScenarioSetBuilder;

public class ScenarioSetBuilderImpl implements ScenarioSetBuilder {
    private List<Scenario<?, ?>> scenarios = new ArrayList<>();;
    
    @Override
    public ScenarioSetBuilder addScenario(Scenario<?, ?> scenario) {
        scenarios.add(scenario);
        return this;
    }

    @Override
    public ScenarioSetBuilder addScenarios(List<Scenario<?,?>> scenarios) {
        for(var scenario: scenarios) {
            this.scenarios.add(scenario);
        }
        return this;
    }


    @Override
    public ScenarioSetBuilder addScenarioLoader(ScenarioLoader scenarioLoader) {
        for (var scenario : scenarioLoader.getScenarios()) {
            this.scenarios.add(scenario);
        }
        return this;
    }


    @Override
    public ScenarioSetBuilder addScenarioLoaders(List<ScenarioLoader> scenarioLoaders) {
        for(var loader: scenarioLoaders) {
            addScenarioLoader(loader);
        }
        
        return this;
    }
    
    @Override
    public ScenarioSet create() {
        return new ScenarioSetImpl(this.scenarios);
    }

}
