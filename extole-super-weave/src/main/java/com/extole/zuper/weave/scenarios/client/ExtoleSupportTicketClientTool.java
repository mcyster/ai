package com.extole.zuper.weave.scenarios.client;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.ToolException;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.client.ExtoleSupportTicketClientScenario.Parameters;

@Component
public class ExtoleSupportTicketClientTool implements Tool<Parameters, ExtoleSuperContext> {
    private AiService aiWeaveService;
    private ExtoleSupportTicketClientScenario extoleTicketClientScenario;

    ExtoleSupportTicketClientTool(AiService aiWeaveService,
            ExtoleSupportTicketClientScenario extoleTicketClientScenario) {
        this.aiWeaveService = aiWeaveService;
        this.extoleTicketClientScenario = extoleTicketClientScenario;
    }

    @Override
    public String getName() {
        return "extoleTicketClient";
    }

    @Override
    public String getDescription() {
        return "Finds the clientId associated with the specified ticket";
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
    public Object execute(Parameters request, ExtoleSuperContext context, Weave weave) throws ToolException {
        ActiveConversation conversation = extoleTicketClientScenario.createConversationBuilder(request, context)
                .addMessage("Ticket Number: " + request.ticketNumber()).start();

        Message message;
        try {
            message = conversation.respond(weave);
        } catch (ConversationException exception) {
            throw new ToolException("findRunbook failed to start conversation", exception);
        }

        System.out.println("!!! find clientId convo: " + message);

        return aiWeaveService.extractResponse(
                com.extole.zuper.weave.scenarios.client.ExtoleSupportTicketClientScenario.Response.class,
                message.getContent());
    }

    public int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass(), extoleTicketClientScenario.hash());
    }

}
