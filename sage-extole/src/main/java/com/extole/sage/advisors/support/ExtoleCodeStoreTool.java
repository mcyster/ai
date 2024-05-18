package com.extole.sage.advisors.support;

import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Component;

import com.cyster.assistant.service.advisor.ToolException;
import com.cyster.store.SimpleVectorStoreService;
import com.extole.sage.advisors.support.ExtoleCodeStoreTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
class ExtoleCodeStoreTool implements ExtoleSupportAdvisorTool<Request> {
    private SimpleVectorStore store;

    ExtoleCodeStoreTool(SimpleVectorStoreService storeService) {
        this.store = storeService.getRepository("extoleCode");
        // TODO verify and warn if empty, perhaps also in tool.execute

    }

    @Override
    public String getName() {
        return "extole_code";
    }

    @Override
    public String getDescription() {
        return "Retrieves public interfaces and classes of the Extole code base";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        return store.similaritySearch(request.query);

    }

    static class Request {
        @JsonPropertyDescription("name of interface, class or attribute to find")
        @JsonProperty(required = true)
        public String query;
    }
}
