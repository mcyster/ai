package com.extole.weave.scenarios.runbooks;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.extole.weave.scenarios.runbooks.ExtoleSupportTicketRunbookSelectorScenario.Parameters;

@Component
public class ExtoleSupportTicketRunbookSelectorTool implements Tool<Parameters, Void> {
    private AiWeaveService aiWeaveService;
    private ExtoleSupportTicketRunbookSelectorScenario runbookSelectorScenario;

    ExtoleSupportTicketRunbookSelectorTool(AiWeaveService aiWeaveService, ExtoleSupportTicketRunbookSelectorScenario runbookSelectorScenario) {
        this.aiWeaveService = aiWeaveService;
        this.runbookSelectorScenario = runbookSelectorScenario;
    }

    @Override
    public String getName() {
        return "extoleTicketRunbookSelector";
    }

    @Override
    public String getDescription() {
        return "Finds the best runbook name for the specified ticket";
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Object execute(Parameters request, Void context, OperationLogger operation) throws ToolException {
        Conversation conversation = runbookSelectorScenario.createConversationBuilder(request, null)
            .addMessage("Ticket Number: " + request.ticketNumber())
            .start();

        Message message;
        try {
            message = conversation.respond(operation);
        } catch (ConversationException exception) {
           throw new ToolException("Find Runbook failed to start conversation", exception);
        }

        return aiWeaveService.extractResponse(com.extole.weave.scenarios.runbooks.ExtoleSupportTicketRunbookSelectorScenario.Response.class, message.getContent());
    }

}

