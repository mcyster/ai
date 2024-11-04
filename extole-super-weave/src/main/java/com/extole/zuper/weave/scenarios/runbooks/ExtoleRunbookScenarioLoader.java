package com.extole.zuper.weave.scenarios.runbooks;

import java.util.List;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;

public interface ExtoleRunbookScenarioLoader extends ScenarioLoader {
    List<RunbookScenario> getRunbookScenarios();
}
