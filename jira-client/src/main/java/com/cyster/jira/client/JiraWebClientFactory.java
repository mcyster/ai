package com.cyster.jira.client;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.function.client.WebClient;

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

    public WebClient getWebClient() {
        if (this.jiraApiKey.isEmpty()) {
            throw new IllegalArgumentException("jiraApiKey is required");
        }

        return JiraWebClientBuilder.builder(this.jiraBaseUri)
            .setApiKey(this.jiraApiKey.get())
            .build();
    }


}
