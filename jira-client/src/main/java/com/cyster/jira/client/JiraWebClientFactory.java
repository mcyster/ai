package com.cyster.jira.client;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.ai.weave.service.advisor.FatalToolException;
import com.cyster.ai.weave.service.advisor.ToolException;

public class JiraWebClientFactory {
    private Optional<String> jiraApiKey = Optional.empty();
    private final String jiraBaseUri;
        
    private static final Logger logger = LogManager.getLogger(JiraWebClientFactory.class);

    public JiraWebClientFactory(String jiraApiKey, String jiraBaseUri) {
        
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
