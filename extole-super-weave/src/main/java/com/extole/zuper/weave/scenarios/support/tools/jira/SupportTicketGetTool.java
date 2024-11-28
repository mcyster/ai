package com.extole.zuper.weave.scenarios.support.tools.jira;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.jira.client.ticket.TicketException;
import com.extole.jira.support.SupportTicket;
import com.extole.jira.support.SupportTicketService;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.zuper.weave.scenarios.support.tools.jira.SupportTicketGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
public class SupportTicketGetTool implements ExtoleSupportTool<Request> {
    private SupportTicketService supportTicketService;

    SupportTicketGetTool(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Retrieve tickets from the Extole Jira support ticket tracking system";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context, OperationLogger operation) throws ToolException {

        if (request.key != null && request.key.isEmpty()) {
            throw new FatalToolException("Attribute ticket key not specified");
        }

        Optional<SupportTicket> ticket;
        try {
            ticket = supportTicketService.getTicket(request.key);
        } catch (TicketException exception) {
            throw new ToolException("Error while loading support ticket: " + request.key, exception);
        }

        if (ticket.isEmpty()) {
            throw new ToolException("Unable to load support ticket: " + request.key);
        }

        return ticket.get();
    }

    static class Request {
        @JsonPropertyDescription("ticket key. of the form LETTERS-NUMBER, e.g. SUP-123, ENG-456")
        @JsonProperty(required = true)
        public String key;
    }
}
