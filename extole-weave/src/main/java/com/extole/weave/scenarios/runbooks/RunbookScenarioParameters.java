package com.extole.weave.scenarios.runbooks;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RunbookScenarioParameters(
    @JsonProperty(required = true) String ticketNumber,
    @JsonProperty(required = true) String runbookName,
    @JsonProperty(required = true) String clientId,
    @JsonProperty(required = true) String clientShortName
) {}
