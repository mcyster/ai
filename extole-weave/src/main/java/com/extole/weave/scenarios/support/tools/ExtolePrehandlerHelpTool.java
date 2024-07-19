package com.extole.weave.scenarios.support.tools;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.extole.weave.scenarios.prehandler.ExtoleJavascriptPrehandlerActionScenario;
import com.extole.weave.scenarios.support.tools.ExtolePrehandlerHelpTool.Request;
import com.extole.weave.session.ExtoleSessionContext;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class ExtolePrehandlerHelpTool implements ExtoleSupportTool<Request> {
    private ExtoleJavascriptPrehandlerActionScenario scenario;
    private ExtoleWebClientFactory extoleWebClientFactory;

    ExtolePrehandlerHelpTool(ExtoleJavascriptPrehandlerActionScenario scenario,
        ExtoleWebClientFactory extoleWebClientFactory) {
        this.scenario = scenario;
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
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

        var prehandlerContext = new ExtoleSessionContext(result.path("access_token").asText());

        try {
            var conversation = scenario.createConversationBuilder(null, prehandlerContext).start();
            conversation.addMessage(Type.USER, request.question);
            return conversation.respond();
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
