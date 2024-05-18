package com.extole.sage.advisors.runbooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cyster.assistant.service.advisor.Tool;
import com.cyster.assistant.service.advisor.Advisor;
import com.cyster.assistant.service.advisor.AdvisorBuilder;
import com.cyster.assistant.service.advisor.AdvisorService;
import com.extole.sage.advisors.support.jira.SupportTicketGetTool;
import com.extole.sage.scenarios.runbooks.ExtoleRunbookOther;

@Component
public class ExtoleRunbookAdvisor implements Advisor<Void> {
    public final String NAME = "extoleRunbookSelector";

    private AdvisorService advisorService;
    private List<Tool<?, Void>> tools = new ArrayList<>();
    private Optional<Advisor<Void>> advisor = Optional.empty();
    private String defaultRunbookName;
    
    public ExtoleRunbookAdvisor(AdvisorService advisorService, ExtoleRunbookSearchTool runbookTool, SupportTicketGetTool ticketGetTool,
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
Interpret the prompt as keywords and use it to create a query
- fix any grammar
- remove duplicate words
- remove PII, URLs, company names
- remove stop words (common words like \"this\", \"is\", \"in\", \"by\", \"with\" etc), 
- normalize the text (convert to lower case and remove special characters)
- keep to 20 words or less.
 
Use the query with the extoleRunbook tool to get the most related Runbooks. 

Review the Runbooks to see which Runbook seems most likely to help with the prompt. 
If no Runbook is a good match use the Runbook name "%s".
 
Respond just in json in the following form { "runbook": "RUNBOOK_NAME" }
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

