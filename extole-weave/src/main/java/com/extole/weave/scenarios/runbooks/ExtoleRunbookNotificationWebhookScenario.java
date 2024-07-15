package com.extole.weave.scenarios.runbooks;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.extole.weave.advisors.support.ExtoleSupportAdvisor;

@Component
public class ExtoleRunbookNotificationWebhookScenario implements RunbookScenario {
    public static String NAME = "extoleRunbookNotificationWebhook";
    private static String DESCRIPTION = "Analyzes and comments on webhook notification tickets";    
    private static String KEYWORDS = "notification webhook";

    private static String INSTRUCTIONS = """
Load the support ticket {{ticket_number}}

Determine the client_id, notification_id (aka event_id) and user_id from https://my.extole.com/notifications/view

Get the notification using the notification_id and user_id to determine its associated attributes.
Get similar client events by searching for client events by user_id and like_noticication_id.

Load the Webhook.

Add a comment to the ticket providing:
- summarizing the notification and attempt to identify the problem with the webhook
- a link to the webhook page https://my.extole.com/tech-center/outbound-webhooks?client_id=$client_id#/$webhook_id
- a link to the notification
- the number of times the related client event has occurred, including the report link

Note the ticket number, and an extremely brief summary of the comment added to the ticket.
""";


    private Advisor<Void> advisor;

    ExtoleRunbookNotificationWebhookScenario(ExtoleSupportAdvisor advisor) {
        this.advisor = advisor;
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
        return this.advisor.createConversation().setOverrideInstructions(INSTRUCTIONS).start();
    }
}

