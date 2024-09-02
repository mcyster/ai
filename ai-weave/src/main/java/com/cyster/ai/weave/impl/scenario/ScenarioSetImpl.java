package com.cyster.ai.weave.impl.scenario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioException;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.cyster.ai.weave.service.scenario.ScenarioSet;
import com.cyster.ai.weave.service.scenario.ScenarioSet;

public class ScenarioSetImpl implements ScenarioSet {
    private Map<String, Scenario<?,?>> scenarios = new HashMap<String, Scenario<?, ?>>();

    public ScenarioSetImpl(List<Scenario<?,?>> scenarios) {
       for(var scenario: scenarios) {
           this.scenarios.put(scenario.getName(), scenario);
       }
    }

    @Override
    public Set<Scenario<?,?>> getScenarios() {
        return scenarios.values().stream().collect(Collectors.toSet());
    }

    @Override
    public boolean hasScenario(String name) {
        return this.scenarios.containsKey(name);
    }

    @Override
    public Scenario<?,?> getScenario(String name) throws ScenarioException {
        if (this.scenarios.containsKey(name)) {
            return this.scenarios.get(name);
        } else {
            throw new ScenarioException("Scenario not found: " + name);
        }
    }
}
