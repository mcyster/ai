package com.cyster.weave.impl.scenarios.tester;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.weave.impl.scenarios.tester.NestedAiTool.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Component
public class NestedAiTool implements Tool<Parameters, Void> {
    private NestedTesterScenario nestedTesterScenario;

    NestedAiTool(AiService aiWeaveService, NestedTesterScenario nestedTesterScenario) {
        this.nestedTesterScenario = nestedTesterScenario;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Asks the specified question of another AI";
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    };

    @Override
    public Object execute(Parameters request, Void context, Weave weave) throws ToolException {
        ActiveConversation conversation = this.nestedTesterScenario.createConversationBuilder(null, null)
                .addMessage(request.prompt).start();

        Message message;
        try {
            message = conversation.respond(weave);
        } catch (ConversationException exception) {
            throw new ToolException("Find Runbook failed to start conversation", exception);
        }

        return message.getContent();
    }

    public int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass(), nestedTesterScenario.hash());
    }

    public static record Parameters(
            @JsonPropertyDescription("Conversatoinal prompt for an AI") @JsonProperty(required = true) String prompt) {
    }

}
