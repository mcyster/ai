package com.extole.sage.advisors.support.jira;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.assistant.impl.advisor.ToolException;
import com.cyster.assistant.service.advisor.FatalToolException;

@Component
public class JiraWebClientFactory {
    private Optional<String> jiraApiKey = Optional.empty();
    private final String jiraBaseUri;
        
    private static final Logger logger = LogManager.getLogger(JiraWebClientFactory.class);

    JiraWebClientFactory(
        @Value("${jiraApiKey:#{environment.JIRA_API_KEY}}") String jiraApiKey,
        @Value("https://extole.atlassian.net/") String jiraBaseUri) {
        
        if (jiraApiKey != null) {
            this.jiraApiKey = Optional.of(jiraApiKey);            
        } else {
            logger.error("jiraApiKey not defined or found in environment.EXTOLE_JIRA_API_KEY");            
        }
        
        this.jiraBaseUri = jiraBaseUri;
    }
    
    public WebClient getWebClient() throws ToolException {
        if (this.jiraApiKey.isEmpty()) {
            throw new FatalToolException("jiraApiKey is required");
        }

        return JiraWebClientBuilder.builder(this.jiraBaseUri)
            .setApiKey(this.jiraApiKey.get())
            .build();
    }
    
 
}
