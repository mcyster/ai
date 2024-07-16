package com.extole.weave.scenarios.runbooks;

import java.io.StringReader;
import java.io.StringWriter;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.extole.weave.scenarios.support.ExtoleSupportHelpScenario;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class ExtoleRunbookNotifiationPrehandlerScenario implements RunbookScenario {
    public static String NAME = "extoleRunbookNotificationPrehandler";
    private static String DESCRIPTION = "Analyzes and comments on prehandler notification tickets";
    private static String KEYWORDS = "notification prehandler";

    private static String INSTRUCTIONS = """
Load the support ticket {{ticket_number}}

Determine the client_id, notification_id (aka event_id) and user_id from https://my.extole.com/notifications/view

Get the notification using the notification_id and user_id to determine its associated attributes.
Get similar client events by searching for client events by user_id and like_noticication_id.

Load the Prehandler.

Add a comment to the ticket providing:
- summarizing the notification and attempt to identify the problem with the prehandler
- a link to the prehandler page https://my.extole.com/labs/prehandlers?client_id=$client_id
- a link to the notification
- the number of times the related client event has occurred, including the report link

Note the ticket number, and an extremely brief summary of the comment added to the ticket.
""";

    private ExtoleSupportHelpScenario helpScenario;

    ExtoleRunbookNotifiationPrehandlerScenario(ExtoleSupportHelpScenario helpScenario) {
        this.helpScenario = helpScenario;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getKeywords() {
        return KEYWORDS;
    }

    @Override
    public Class<RunbookScenarioParameters> getParameterClass() {
        return RunbookScenarioParameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }
    
    @Override
    public Conversation createConversation(RunbookScenarioParameters parameters, Void context) {
        throw new UnsupportedOperationException("Method is deprectated and being removed from interface");
    }

    @Override
    public ConversationBuilder createConversationBuilder(RunbookScenarioParameters parameters, Void context) {
        MustacheFactory mostacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mostacheFactory.compile(new StringReader(INSTRUCTIONS), "instructions");
        var messageWriter = new StringWriter();
        mustache.execute(messageWriter, parameters);
        messageWriter.flush();
        
        var instructions = messageWriter.toString();

        return this.helpScenario.createConversationBuilder(null, null).setOverrideInstructions(instructions);
    }
}

