package com.extole.zuper.weave.scenarios.runbooks;

import java.util.List;

import com.cyster.ai.weave.service.scenario.ScenarioLoader;

public interface ExtoleRunbookScenarioLoader extends ScenarioLoader {
    List<RunbookSuperScenario> getRunbookScenarios();
}
