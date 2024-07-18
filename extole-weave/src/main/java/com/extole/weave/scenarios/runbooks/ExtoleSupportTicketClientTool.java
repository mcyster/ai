package com.extole.weave.scenarios.runbooks;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;

import com.extole.weave.scenarios.runbooks.ExtoleSupportTicketClientScenario.Parameters;

@Component
public class ExtoleSupportTicketClientTool implements Tool<Parameters, Void> {
    private AiWeaveService aiWeaveService;
    private ExtoleSupportTicketClientScenario extoleTicketClientScenario;

    ExtoleSupportTicketClientTool(AiWeaveService aiWeaveService, ExtoleSupportTicketClientScenario extoleTicketClientScenario) {
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
    public Object execute(Parameters request, Void context) throws ToolException {
        Conversation conversation = extoleTicketClientScenario.createConversationBuilder(request, null)
            .addMessage("Ticket Number: " + request.ticketNumber())
            .start();

        Message message;
        try {
            message = conversation.respond();
        } catch (ConversationException exception) {
           throw new ToolException("findRunbook failed to start conversation", exception);
        }

        System.out.println("!!! find clientId convo: " + message);
        
        return aiWeaveService.extractResponse(com.extole.weave.scenarios.runbooks.ExtoleSupportTicketClientScenario.Response.class, message.getContent());
    }
   
}

