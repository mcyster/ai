package com.extole.zuper.weave.scenarios.jsonpath;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.admin.weave.scenarios.jsonpath.ExtoleEventStreamJsonPathScenario;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.extole.client.web.ExtoleClientApiKey;
import com.extole.client.web.ExtoleWebClientException;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.jsonpath.ExtoleEventStreamJsonPathSuperScenario.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleEventStreamJsonPathSuperScenario implements Scenario<Parameters, ExtoleSuperContext> {
    private ExtoleEventStreamJsonPathScenario adminScenario;

    public ExtoleEventStreamJsonPathSuperScenario(ExtoleEventStreamJsonPathScenario adminScenario) {
        this.adminScenario = adminScenario;
    }

    @Override
    public String getName() {
        return this.adminScenario.getName() + "Filter";
    }

    @Override
    public String getDescription() {
        return this.adminScenario.getDescription();
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public ActiveConversationBuilder createConversationBuilder(Parameters parameters, ExtoleSuperContext context) {
        var adminToolParameters = new ExtoleEventStreamJsonPathScenario.Parameters(parameters.eventStreamId,
                parameters.jsonPathQuery);

        String adminToken;
        try {
            adminToken = new ExtoleClientApiKey().getClientApiKey(context.superToken(), parameters.clientId());
        } catch (ExtoleWebClientException exception) {
            throw new RuntimeException("Error unable to get client key for client: " + parameters.clientId(),
                    exception);
        }

        ExtoleSessionContext adminContext = new ExtoleSessionContext(adminToken);

        return this.adminScenario.createConversationBuilder(adminToolParameters, adminContext);
    };

    public static record Parameters(@JsonProperty(required = true) String clientId,
            @JsonProperty(required = true) String eventStreamId, @JsonProperty(required = false) String jsonPathQuery) {
        public Parameters(String clientId, String eventStreamId) {
            this(clientId, eventStreamId, null);
        }
    };
}
