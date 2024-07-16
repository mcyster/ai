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
public class ExtoleRunbookNotificationTrafficIncreaseScenario implements RunbookScenario {
    public static final String NAME = "extoleRunbookNotificationTrafficIncrease";
    private static final String DESCRIPTION = "Analyzes and comments on traffic increase notification tickets";
    private static final String KEYWORDS = "notification traffic increase automatic change percentage alerts";

    private static final String INSTRUCTIONS = """
Load the support ticket {{ticket_number}}

Determine the client_id, notification_id (aka event_id) and user_id from https://my.extole.com/notifications?client_id={{client_id}}&user_id={{user_id}}#/view/{{notification_id}}

Get the notification using the notification_id and user_id to determine its associated attributes.
Get similar client events by searching for client events by user_id and like_notification_id.
Get the traffic to the top promotion sources.
Run all the activity insight tools.

Add a comment to the ticket providing:
- a summarization the description, group by program with a sublist of the fields and the amount by which each field changed
- a link to the notification
- the number of times the related client event has occurred, including the report link
- the traffic to the to top promotion sources, including the report link
- list the links returned by the the activity insight tools.

Note the ticket number, and an extremely brief summary of the comment added to the ticket.
""";

    private ExtoleSupportHelpScenario helpScenario;

    ExtoleRunbookNotificationTrafficIncreaseScenario(ExtoleSupportHelpScenario helpScenario) {
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

