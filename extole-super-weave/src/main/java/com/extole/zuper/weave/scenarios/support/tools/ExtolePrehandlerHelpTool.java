package com.extole.zuper.weave.scenarios.support.tools;

import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.client.web.ExtoleWebClientException;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.prehandler.ExtoleJavascriptPrehandlerActionScenario;
import com.extole.zuper.weave.scenarios.support.tools.ExtolePrehandlerHelpTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class ExtolePrehandlerHelpTool implements ExtoleSupportTool<Request> {
    private ExtoleJavascriptPrehandlerActionScenario scenario;
    private ExtoleTrustedWebClientFactory extoleWebClientFactory;

    ExtolePrehandlerHelpTool(ExtoleJavascriptPrehandlerActionScenario scenario,
            ExtoleTrustedWebClientFactory extoleWebClientFactory) {
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
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public Object execute(Request request, ExtoleSuperContext context, Weave weave) throws ToolException {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        {
            payload.put("client_id", request.client_id);
        }

        JsonNode result;
        try {
            result = this.extoleWebClientFactory.getSuperUserWebClient().post()
                    .uri(uriBuilder -> uriBuilder.path("/v4/tokens").build()).accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON).bodyValue(payload).retrieve().bodyToMono(JsonNode.class)
                    .block();
        } catch (ExtoleWebClientException | WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get clients");
        }

        if (result == null || !result.has("access_token")) {
            throw new ToolException("Unable to get client specific access token");
        }

        try {
            var conversation = scenario.createConversationBuilder(null, context).start();
            conversation.addMessage(Type.USER, request.question);
            return conversation.respond(weave);
        } catch (ConversationException exception) {
            throw new ToolException("", exception);
        }
    }

    public int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass(), scenario.hash());
    }

    static class Request {
        @JsonProperty(required = true)
        public String client_id;

        @JsonPropertyDescription("Question about prehandler, including code snippets or a prehandler id")
        @JsonProperty(required = true)
        public String question;
    }
}
