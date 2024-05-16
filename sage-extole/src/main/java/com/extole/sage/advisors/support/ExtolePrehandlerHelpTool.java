package com.extole.sage.advisors.support;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.assistant.impl.advisor.ToolException;
import com.cyster.assistant.service.advisor.FatalToolException;
import com.cyster.assistant.service.conversation.ConversationException;
import com.extole.sage.advisors.ExtoleJavascriptPrehandlerActionAdvisor;
import com.extole.sage.advisors.ExtoleJavascriptPrehandlerActionAdvisor.AdminUserToolContext;
import com.extole.sage.advisors.support.ExtolePrehandlerHelpTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class ExtolePrehandlerHelpTool implements ExtoleSupportAdvisorTool<Request> {
    private ExtoleJavascriptPrehandlerActionAdvisor advisor;
    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtolePrehandlerHelpTool(ExtoleJavascriptPrehandlerActionAdvisor advisor,
        ExtoleWebClientFactory extoleWebClientFactory) {
        this.advisor = advisor;
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extole_prehandler_help";
    }

    @Override
    public String getDescription() {
        return "Provides help with Extole prehandlers";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            payload.put("client_id", request.client_id);
        }

        JsonNode result;
        try {
            result = this.extoleWebClientFactory.getSuperUserWebClient().post()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/tokens")
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get clients");
        }

        if (result == null || !result.has("access_token")) {
            throw new ToolException("Unable to get client specific access token");
        }

        var prehandlerContext = new AdminUserToolContext(result.path("access_token").asText());

        try {
            return advisor.createConversation().withContext(prehandlerContext).start().addMessage(request.question)
                .respond();
        } catch (ConversationException exception) {
            throw new ToolException("", exception);
        }
    }

    static class Request {
        @JsonProperty(required = true)
        public String client_id;

        @JsonPropertyDescription("Question about prehandler, including code snippets or a prehandler id")
        @JsonProperty(required = true)
        public String question;
    }
}
