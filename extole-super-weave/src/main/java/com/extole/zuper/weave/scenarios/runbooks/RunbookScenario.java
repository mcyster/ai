package com.extole.zuper.weave.scenarios.runbooks;

import com.cyster.ai.weave.service.scenario.Scenario;

public interface RunbookScenario extends Scenario<RunbookScenarioParameters, Void> {
    String getName();
    String getKeywords();
}
