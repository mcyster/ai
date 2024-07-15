package com.extole.weave;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.cyster.jira.client.JiraWebClientFactory;


@Configuration
public class ExtoleSageConfig {

    @Bean
    public JiraWebClientFactory getJiraWebClientFactory(
        @Value("${JIRA_API_KEY}") String jiraApiKey,
        @Value("https://extole.atlassian.net/") String jiraBaseUri) {

        if (!StringUtils.hasText(jiraApiKey)) {
            throw new IllegalArgumentException("JIRA_API_KEY not defined");
        }

        return new JiraWebClientFactory(jiraApiKey, jiraBaseUri);
    }
}