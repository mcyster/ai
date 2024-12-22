package com.extole.zuper.weave.scenarios.runbooks;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.extole.zuper.weave.ExtoleSuperContext;
import com.extole.zuper.weave.scenarios.runbooks.ExtoleSupportTicketRunbookSelectorScenario.Parameters;

@Component
public class ExtoleSupportTicketRunbookSelectorTool implements Tool<Parameters, ExtoleSuperContext> {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleSupportTicketRunbookSelectorTool.class);

    private AiWeaveService aiWeaveService;
    private ExtoleSupportTicketRunbookSelectorScenario runbookSelectorScenario;

    ExtoleSupportTicketRunbookSelectorTool(AiWeaveService aiWeaveService,
            ExtoleSupportTicketRunbookSelectorScenario runbookSelectorScenario) {
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
    public Class<ExtoleSuperContext> getContextClass() {
        return ExtoleSuperContext.class;
    }

    @Override
    public Object execute(Parameters request, ExtoleSuperContext context, Weave weave) throws ToolException {
        ActiveConversation conversation = runbookSelectorScenario.createConversationBuilder(request, context)
                .addMessage("Ticket Number: " + request.ticketNumber()).start();

        Message message;
        try {
            message = conversation.respond(weave);
        } catch (ConversationException exception) {
            throw new ToolException("Find Runbook failed to start conversation", exception);
        }

        logger.info("Runbook Selection for context: " + request + " in context " + context + " got: "
                + message.getContent());

        return aiWeaveService.extractResponse(
                com.extole.zuper.weave.scenarios.runbooks.ExtoleSupportTicketRunbookSelectorScenario.Response.class,
                message.getContent());
    }

    public int hash() {
        return Objects.hash(getName(), getDescription(), getParameterClass(), runbookSelectorScenario.hash());
    }
}
