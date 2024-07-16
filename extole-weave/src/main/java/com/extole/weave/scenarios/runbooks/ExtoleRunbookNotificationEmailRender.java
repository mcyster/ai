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
public class ExtoleRunbookNotificationEmailRender implements RunbookScenario {
    public static String NAME = "extoleRunbookNotificationEmailRender";
    private static String DESCRIPTION = "Analyzes and comments on email notification tickets";
    private static String KEYWORDS = "notification  email render";

    private static String INSTRUCTIONS = """
Load the support ticket {{ticket_number}}

Determine the client_id, notification_id (aka event_id) and user_id from https://my.extole.com/notifications/view

Get the notification using the notification_id and user_id to determine its associated attributes.
Get similar client events by searching for client events by user_id and like_noticication_id.

From the notification get the campaign_id and zone_name and person_id (sender_person_id)

Add a comment to the ticket providing:
- summarizing the notification and attempt to identify the problem with the email
- mention the zone name
- link to the campaign https://my.extole.com/campaign/edit/?client_id=$client_id#/$campaign_id
- link to the person https://my.extole.com/profiles/view?client_id=$client_id#$person_id
- link to the notification
- include the number of times the related client event has occurred, including the report link

Note the ticket number, and an extremely brief summary of the comment added to the ticket.
""";

    private ExtoleSupportHelpScenario helpScenario;

    ExtoleRunbookNotificationEmailRender(ExtoleSupportHelpScenario helpScenario) {
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

