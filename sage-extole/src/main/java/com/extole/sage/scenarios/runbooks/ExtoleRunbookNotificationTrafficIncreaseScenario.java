package com.extole.sage.scenarios.runbooks;

import org.springframework.stereotype.Component;

import com.cyster.sherpa.service.advisor.Advisor;
import com.cyster.sherpa.service.conversation.Conversation;
import com.extole.sage.advisors.support.ExtoleSupportAdvisor;

@Component
public class ExtoleRunbookNotificationTrafficIncreaseScenario implements RunbookScenario {
    public static String NAME = "extoleRunbookNotificationTrafficIncrease";
    private static String DESCRIPTION = "Analyzes and comments on traffic increase notification tickets";    
    private static String KEYWORDS = "notification traffic increase automatic change percentage alerts";

    private static String INSTRUCTIONS = """
Load the support ticket {{ticket_number}}

Determine the client_id, notification_id (aka event_id) and user_id from https://my.extole.com/notifications/view

Get the notification using the notification_id and user_id to determine its associated attributes.
Get similar client events by searching for client events by user_id and like_noticication_id.
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


    private Advisor<Void> advisor;

    ExtoleRunbookNotificationTrafficIncreaseScenario(ExtoleSupportAdvisor advisor) {
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

