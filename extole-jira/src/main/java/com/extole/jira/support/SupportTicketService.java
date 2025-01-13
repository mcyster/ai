package com.extole.jira.support;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.cyster.jira.client.ticket.TicketAttachmentBuilder;
import com.cyster.jira.client.ticket.TicketCommentBuilder;
import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.ticket.TicketQueryBuilder;
import com.cyster.jira.client.ticket.TicketService;
import com.cyster.jira.client.ticket.TicketServiceFactory;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@Component
public class SupportTicketService implements TicketService<SupportTicket> {
    private final JiraWebClientFactory jiraWebClientFactory;
    private final TicketService<SupportTicket> ticketService;
    private final SupportTicketOrganizations supportTicketOrganizations;
    private final SupportTicketClients supportTicketClients;

    SupportTicketService(TicketServiceFactory ticketServiceFactory, JiraWebClientFactory jiraWebClientFactory,
            SupportTicketOrganizations supportTicketOrganization, SupportTicketClients supportTicketClients) {
        this.jiraWebClientFactory = jiraWebClientFactory;
        this.ticketService = ticketServiceFactory.createTicketService(new SupportTicketMapper());
        this.supportTicketOrganizations = supportTicketOrganization;
        this.supportTicketClients = supportTicketClients;
    }

    @Override
    public TicketQueryBuilder<SupportTicket> ticketQueryBuilder() {
        return ticketService.ticketQueryBuilder();
    }

    @Override
    public Optional<SupportTicket> getTicket(String key) throws TicketException {
        return ticketService.getTicket(key);
    }

    @Override
    public TicketAttachmentBuilder ticketAttachmentBuilder(String key) {
        return ticketService.ticketAttachmentBuilder(key);
    }

    @Override
    public TicketCommentBuilder ticketCommentBuilder(String key) {
        return ticketService.ticketCommentBuilder(key);
    }

    public void setClient(String ticketNumber, String clientShortName) throws TicketException {
        if (ticketNumber.startsWith("HELP")) {
            setClientAsOrganization(ticketNumber, clientShortName);
        } else {
            setClientAsCustomField(ticketNumber, clientShortName);
        }
    }

    private void setClientAsCustomField(String ticketNumber, String clientShortName) throws TicketException {
        var jiraClientId = supportTicketClients.getJiraClientIndexForClientShortName(clientShortName);

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            ObjectNode fields = JsonNodeFactory.instance.objectNode();
            ObjectNode customField = JsonNodeFactory.instance.objectNode();

            customField.set("id", JsonNodeFactory.instance.textNode(jiraClientId.toString()));
            fields.set("customfield_11312", customField);

            payload.set("fields", fields);

        }

        try {
            this.jiraWebClientFactory.getWebClient().put()
                    .uri(uriBuilder -> uriBuilder.path("/rest/api/3/issue/" + ticketNumber)
                            .queryParam("notifyUsers", "false").build())
                    .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).bodyValue(payload)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> response
                            .bodyToMono(String.class)
                            .map(errorBody -> new TicketException("Problems putting client " + clientShortName
                                    + " to ticket " + ticketNumber + " Bad request code: " + response.statusCode()
                                    + " body: " + errorBody + " payload:" + payload.toString()))
                            .flatMap(Mono::error))
                    .toBodilessEntity().block();
        } catch (Throwable exception) {
            if (exception.getCause() instanceof TicketException) {
                throw (TicketException) exception.getCause();
            }
            throw exception;
        }
    }

    private void setClientAsOrganization(String ticketNumber, String clientShortName) throws TicketException {
        var organizationIndex = supportTicketOrganizations.getJiraOrganizationIndexFromClientShortName(clientShortName);

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            ObjectNode fields = JsonNodeFactory.instance.objectNode();

            ArrayNode values = JsonNodeFactory.instance.arrayNode();
            values.add(organizationIndex);
            fields.set("customfield_11100", values);

            payload.set("fields", fields);
        }

        try {
            this.jiraWebClientFactory.getWebClient().put()
                    .uri(uriBuilder -> uriBuilder.path("/rest/api/3/issue/" + ticketNumber)
                            .queryParam("notifyUsers", "false").build())
                    .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).bodyValue(payload)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response -> response
                            .bodyToMono(String.class)
                            .map(errorBody -> new TicketException("Problems posting organization " + organizationIndex
                                    + " to ticket" + "Bad request code: " + response.statusCode() + " body: "
                                    + errorBody + " payload:" + payload.toString()))
                            .flatMap(Mono::error))
                    .toBodilessEntity().block();
        } catch (Throwable exception) {
            if (exception.getCause() instanceof TicketException) {
                throw (TicketException) exception.getCause();
            }
            throw exception;
        }
    }

}
