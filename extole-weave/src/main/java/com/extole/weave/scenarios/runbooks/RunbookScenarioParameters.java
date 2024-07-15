package com.extole.weave.scenarios.runbooks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RunbookScenarioParameters {
    private String ticketNumber;

    @JsonCreator
    public RunbookScenarioParameters(@JsonProperty("ticketNumber") String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    @JsonProperty(required = true)
    public String getTicketNumber() {
        return this.ticketNumber;
    }
}
