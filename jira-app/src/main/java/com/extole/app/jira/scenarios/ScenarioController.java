package com.extole.app.jira.scenarios;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioException;
import com.cyster.ai.weave.service.scenario.ScenarioService;

@RestController
public class ScenarioController {
    private ScenarioService scenarioStore;

    public ScenarioController(ScenarioService scenarioStore) {
        this.scenarioStore = scenarioStore;
    }

    @GetMapping("/scenarios")
    public Set<ScenarioResponse> index() {
        return scenarioStore.getScenarios().stream().map(scenario -> new ScenarioResponse.Builder()
            .setName(scenario.getName())
            .setDescription(scenario.getDescription())
            .setParameterClass(scenario.getParameterClass())
            .build()).collect(Collectors.toSet());
    }

    @GetMapping("/scenarios/{scenario_name}")
    public ScenarioResponse get(@PathVariable("scenario_name") String scenarioName) throws ScenarioNotFoundException {
        Scenario<?, ?> scenario;
        try {
            scenario = scenarioStore.getScenario(scenarioName);
        } catch (ScenarioException e) {
           throw new ScenarioNotFoundException("Not found: " + scenarioName);
        }
        
        return new ScenarioResponse.Builder()
            .setName(scenario.getName())
            .setDescription(scenario.getDescription())
            .setParameterClass(scenario.getParameterClass())
            .build();
    }
}
