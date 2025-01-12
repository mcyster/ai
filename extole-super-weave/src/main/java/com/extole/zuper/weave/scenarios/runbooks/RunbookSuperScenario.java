package com.extole.zuper.weave.scenarios.runbooks;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.zuper.weave.ExtoleSuperContext;

public interface RunbookSuperScenario extends Scenario<RunbookScenarioParameters, ExtoleSuperContext> {
    String getName();

    String getKeywords();
}
