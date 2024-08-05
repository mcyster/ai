package com.extole.weave.scenarios.web.devloper.tools;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.web.weave.scenarios.ManagedWebsites;
import com.cyster.web.weave.scenarios.WebsiteDeveloperTool;
import com.extole.weave.scenarios.support.tools.ExtoleReportSchemaTool;
import com.extole.weave.scenarios.support.tools.ExtoleReportSchemaTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

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
    public Object execute(Request request, ManagedWebsites context, OperationLogger operation) throws ToolException {
        return this.reportSchemaTool.execute(request, null, operation);
    }


}
