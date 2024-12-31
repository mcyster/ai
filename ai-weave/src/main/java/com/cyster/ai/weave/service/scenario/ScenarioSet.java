package com.cyster.ai.weave.service.scenario;

import java.util.Set;

public interface ScenarioSet<CONTEXT> {

    Set<Scenario<?, CONTEXT>> getScenarios();

    boolean hasScenario(String name);

    Scenario<?, CONTEXT> getScenario(String name) throws ScenarioException;
}
