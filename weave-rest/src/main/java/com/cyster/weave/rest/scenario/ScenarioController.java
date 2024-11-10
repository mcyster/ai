package com.cyster.weave.rest.scenario;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioException;
import com.cyster.ai.weave.service.scenario.ScenarioSet;

@RestController
public class ScenarioController {
    private static final Logger logger = LoggerFactory.getLogger(ScenarioController.class);

    private ScenarioSet scenarioStore;

    public ScenarioController(ScenarioSet scenarioStore) {
        this.scenarioStore = scenarioStore;
    }

    @GetMapping("/scenarios")
    public Set<ScenarioResponse> index() {
        logger.debug("List scenarios");

        return scenarioStore.getScenarios().stream()
                .map(scenario -> new ScenarioResponse.Builder().setName(scenario.getName())
                        .setDescription(scenario.getDescription()).setParameterClass(scenario.getParameterClass())
                        .build())
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ScenarioResponse::getName))));

    }

    @GetMapping("/scenarios/{scenario_name}")
    public ScenarioResponse get(@PathVariable("scenario_name") String scenarioName) throws ScenarioNotFoundException {
        Scenario<?, ?> scenario;
        try {
            scenario = scenarioStore.getScenario(scenarioName);
        } catch (ScenarioException e) {
            throw new ScenarioNotFoundException("Not found: " + scenarioName);
        }

        return new ScenarioResponse.Builder().setName(scenario.getName()).setDescription(scenario.getDescription())
                .setParameterClass(scenario.getParameterClass()).build();
    }
}
