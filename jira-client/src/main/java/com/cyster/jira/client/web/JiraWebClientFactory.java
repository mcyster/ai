package com.cyster.jira.client.web;

import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class JiraWebClientFactory {    
    private Optional<String> jiraApiKey = Optional.empty();
    private final String jiraBaseUri;

    private static final Logger logger = LoggerFactory.getLogger(JiraWebClientFactory.class);

    public JiraWebClientFactory(@Value("${JIRA_API_KEY}") String jiraApiKey, @Value("https://extole.atlassian.net/") String jiraBaseUri) {

        if (jiraApiKey != null) {
            this.jiraApiKey = Optional.of(jiraApiKey);
        } else {
            logger.error("jiraApiKey not defined or found in environment.EXTOLE_JIRA_API_KEY");
        }

        this.jiraBaseUri = jiraBaseUri;
    }

    public WebClient getWebClient() {
        if (this.jiraApiKey.isEmpty()) {
            throw new IllegalArgumentException("jiraApiKey is required");
        }
        
        return JiraWebClientBuilder.builder(this.jiraBaseUri)
            .setApiKey(this.jiraApiKey.get())
            .build();
    }


}
