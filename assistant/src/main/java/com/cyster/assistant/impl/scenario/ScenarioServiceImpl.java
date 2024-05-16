package com.cyster.assistant.impl.scenario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.cyster.assistant.impl.advisor.AdvisorServiceImpl;
import com.cyster.assistant.service.advisor.AdvisorService;
import com.cyster.assistant.service.advisor.AdvisorServiceFactory;
import com.cyster.assistant.service.scenario.Scenario;
import com.cyster.assistant.service.scenario.ScenarioException;
import com.cyster.assistant.service.scenario.ScenarioLoader;
import com.cyster.assistant.service.scenario.ScenarioService;
import com.cyster.assistant.service.scenario.ScenarioServiceFactory;

public class ScenarioServiceImpl implements ScenarioService {
    private Map<String, Scenario<?,?>> scenarios = new HashMap<String, Scenario<?, ?>>();

    public ScenarioServiceImpl(List<ScenarioLoader> scenarioLoaders, List<Scenario<?,?>> scenarios) {
        for (var scenario : scenarios) {
            this.scenarios.put(scenario.getName(), scenario);
        }
        
        for(var loader: scenarioLoaders) {
            for (var scenario : loader.getScenarios()) {
                this.scenarios.put(scenario.getName(), scenario);
            }
        }
    }

    public Set<Scenario<?,?>> getScenarios() {
        return scenarios.values().stream().collect(Collectors.toSet());
    }

    public Scenario<?,?> getScenario(String name) throws ScenarioException {
        if (this.scenarios.containsKey(name)) {
            return this.scenarios.get(name);
        } else {
            throw new ScenarioException("Scenario not found: " + name);
        }
    }
    
    public static class Factory implements ScenarioServiceFactory {
        @Override
        public ScenarioService createScenarioService(List<ScenarioLoader> loaders, List<Scenario<?,?>> scenarios) {
            return new ScenarioServiceImpl(loaders, scenarios);
        }
    }
}
