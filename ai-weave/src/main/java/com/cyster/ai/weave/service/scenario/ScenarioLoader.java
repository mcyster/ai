package com.cyster.ai.weave.service.scenario;

import java.util.List;

public interface ScenarioLoader<CONTEXT> {
    List<Scenario<?, CONTEXT>> getScenarios();
}
