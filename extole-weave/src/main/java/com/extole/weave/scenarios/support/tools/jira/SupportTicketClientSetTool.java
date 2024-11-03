package com.extole.weave.scenarios.support.tools.jira;


import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.ToolException;
import com.extole.jira.support.SupportTicketException;
import com.extole.jira.support.SupportTicketService;
import com.extole.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.weave.scenarios.support.tools.jira.SupportTicketClientSetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class SupportTicketClientSetTool implements ExtoleSupportTool<Request> {
	public static final String CLIENT_NOT_FOUND = "not_found";
	
    private static final Logger logger = LogManager.getLogger(SupportTicketClientSetTool.class);

    private SupportTicketService supportTicketService;

    SupportTicketClientSetTool(SupportTicketService supportTicketService,
        @Value("${JIRA_TEST_MODE:false}") boolean testMode) {
        this.supportTicketService = supportTicketService;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Set the client (organization) associated with a ticket";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context, OperationLogger operation) throws ToolException {
        if (request.key == null || request.key.isEmpty()) {
            throw new ToolException("Attribute 'key' not specified");
        }

        if (request.clientShortName == null || request.clientShortName.isEmpty()) {
            throw new ToolException("Attribute 'clientShortName' must be specified");
        }

        if (request.clientShortName.equals(CLIENT_NOT_FOUND)) {
            ObjectNode response = JsonNodeFactory.instance.objectNode();
            response.put("key", request.key);
            return response;
        }
        
        try {
			supportTicketService.setClient(request.key, request.clientShortName);
		} catch (SupportTicketException exception) {
			throw new ToolException("Unable to set clientShortName: " + request.clientShortName + " on ticket: " + request.key, exception);
		}

        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.put("key", request.key);
        return response;
    }
   
    public int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass());
    }
    
    static class Request {
        @JsonPropertyDescription("ticket key")
        @JsonProperty(required = true)
        public String key;

        @JsonPropertyDescription("Client Short Name")
        @JsonProperty(required = true)
        public String clientShortName;
    }
}
