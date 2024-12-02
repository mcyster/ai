package com.extole.admin.weave.scenarios.wismr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.Tool;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ExtoleStepsToolParameters {
    private String actionName;

    public ExtoleStepsToolParameters(@JsonProperty("actionName") String actionName) {
        this.actionName = actionName;
    }

    @JsonProperty("actionName")
    public String getPersonId() {
        return this.actionName;
    }
}

class ExtoleStepsTool implements Tool<ExtoleStepsToolParameters, Void> {

    public ExtoleStepsTool() {
    }

    @Override
    public String getName() {
        return "steps_by_action";
    }

    @Override
    public String getDescription() {
        return "Get step names that perform an action, for example actionName: earnReward";
    }

    @Override
    public Class<ExtoleStepsToolParameters> getParameterClass() {
        return ExtoleStepsToolParameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Object execute(ExtoleStepsToolParameters parameters, Void context, OperationLogger operation) {
        return this.getExecutor().apply((ExtoleStepsToolParameters) parameters);
    }

    public Function<ExtoleStepsToolParameters, Object> getExecutor() {
        return parameter -> loadSteps(parameter);
    }

    private JsonNode loadSteps(ExtoleStepsToolParameters parameters) {
        List<String> stepNames = new ArrayList<String>();

        stepNames.add("conversion");

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(stepNames);
    }

}

@Component
public class ExtoleStepsToolFactory {
    private final WebClient.Builder webClientBuilder;

    public ExtoleStepsToolFactory(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    ExtolePersonStepsTool create(Optional<String> accessToken) {
        return new ExtolePersonStepsTool(this.webClientBuilder, accessToken);
    }
}
