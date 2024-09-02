package com.cyster.ai.weave.service.scenario;

import java.util.Set;

public interface ScenarioSet {

    Set<Scenario<?,?>> getScenarios();

    boolean hasScenario(String name);

    Scenario<?,?> getScenario(String name) throws ScenarioException;
}
