package com.extole.weave.scenarios.web.devloper.tools;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.ToolException;
import com.cyster.web.weave.scenarios.ManagedWebsites;
import com.cyster.web.weave.scenarios.WebsiteDeveloperTool;
import com.extole.weave.scenarios.support.tools.ExtoleReportSchemaTool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import com.extole.weave.scenarios.web.devloper.tools.ExtoleWebDeveloperReportSchemaTool.Request;

@Component
public class ExtoleWebDeveloperReportSchemaTool implements WebsiteDeveloperTool<Request> {
    private ExtoleReportSchemaTool reportSchemaTool;
        
    ExtoleWebDeveloperReportSchemaTool(ExtoleReportSchemaTool reportSchemaTool) {
        this.reportSchemaTool = reportSchemaTool;
    }
    
    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Get the json schema of a report given the Extole client id and report id";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request parameters, ManagedWebsites context) throws ToolException {
        var request = new ExtoleReportSchemaTool.Request(parameters.clientId, parameters.reportId);
        return this.reportSchemaTool.execute(request, null);
    }

    static record Request(
        @JsonPropertyDescription("The 1 to 12 digit id for a client.")
        @JsonProperty(required = true)
        String clientId,

        @JsonPropertyDescription("The 20 to 22 character alphanumeric Extole report id")
        @JsonProperty(required = true)
        String reportId
    ) {}
}
