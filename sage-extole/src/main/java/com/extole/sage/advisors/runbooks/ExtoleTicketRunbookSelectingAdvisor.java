package com.extole.sage.advisors.runbooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.assistant.impl.advisor.Tool;
import com.cyster.assistant.service.advisor.Advisor;
import com.cyster.assistant.service.advisor.AdvisorBuilder;
import com.cyster.assistant.service.advisor.AdvisorService;
import com.extole.sage.advisors.support.jira.SupportTicketGetTool;
import com.extole.sage.scenarios.runbooks.ExtoleRunbookOther;

@Component
public class ExtoleTicketRunbookSelectingAdvisor implements Advisor<Void> {
    public final String NAME = "extoleTicketRunbookSelector";

    private AdvisorService advisorService;
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private Optional<Advisor<Void>> advisor = Optional.empty();
    private String defaultRunbookName;
    
    public ExtoleTicketRunbookSelectingAdvisor(AdvisorService advisorService, ExtoleRunbookSearchTool runbookTool, SupportTicketGetTool ticketGetTool,
        ExtoleRunbookOther defaultRunbook) {
        this.advisorService = advisorService;
        this.tools.add(runbookTool);
        this.tools.add(ticketGetTool);
        this.defaultRunbookName = defaultRunbook.getName();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ConversationBuilder<Void> createConversation() {
        if (this.advisor.isEmpty()) {
            String instructions = """ 
You are an Extole Support Team member handling an incoming ticket. Your task is to identify the most appropriate Runbook for resolving the ticket's issue.

Ticket Information: Use the ticketGet to get the specified ticket

Query Preparation:

Construct a query string using the ticket's classification, title, and description.
Edit the query to:
- Correct grammar mistakes.
- Eliminate duplicate words and personal identifiable information (PII), URLs, and company names.
- Remove common stop words (e.g., "this", "is", "in").
- Convert all text to lowercase and remove special characters.
- Limit the query to 20 words or fewer.

Runbook Search: Utilize the extoleRunbookSearch tool with your prepared query.

Runbook Selection:

From the search results, choose the Runbook that best fits the ticket's needs.
If no suitable Runbook is found, use the default "%s" Runbook.
Response: Provide your answer in JSON format, like so:
{ 
  "ticket_number": "NUMBER", 
  "runbook": "RUNBOOK_NAME", 
  "query": "QUERY" 
}
""";

            AdvisorBuilder<Void> builder = this.advisorService.getOrCreateAdvisor(NAME);
            builder
                .setInstructions(String.format(instructions, this.defaultRunbookName));
                
           for(var tool: tools) {
                builder.withTool(tool);
           }

            this.advisor = Optional.of(builder.getOrCreate());
        }
        return this.advisor.get().createConversation();
    }

}

