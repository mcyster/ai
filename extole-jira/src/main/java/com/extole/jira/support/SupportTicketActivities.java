package com.extole.jira.support;

import com.cyster.jira.client.web.JiraWebClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SupportTicketActivities {

    private final JiraWebClientFactory jiraWebClientFactory;

    public SupportTicketActivities(JiraWebClientFactory jiraWebClientFactory) {
        this.jiraWebClientFactory = jiraWebClientFactory;
    }
    
    public List<Activity> getActivities() {
        String customFieldId = "customfield_11392";
        int maxResults = 100;
        int startAt = 0;
        List<JsonNode> allOptions = new ArrayList<>();

        JsonNode contextResponse = jiraWebClientFactory.getWebClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/rest/api/3/field/{customFieldId}/context")
                        .queryParam("expand", "option")
                        .build(customFieldId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        String contextId = contextResponse.get("values").get(0).get("id").asText();

        boolean isLast = false;
        while (!isLast) {
        	final int start = startAt;
            JsonNode optionsResponse = jiraWebClientFactory.getWebClient().get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/api/3/field/{customFieldId}/context/{contextId}/option")
                            .queryParam("maxResults", maxResults)
                            .queryParam("startAt", start)
                            .build(customFieldId, contextId))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode values = optionsResponse.get("values");
            if (values != null && values.isArray()) {
                values.forEach(allOptions::add);
            }

            isLast = optionsResponse.get("isLast").asBoolean();
            if (!isLast) {
                startAt += maxResults;
            }
        }

        Map<String, String> categoryMap = new HashMap<>();
        for (JsonNode option : allOptions) {
            if (!option.has("optionId")) {
                String id = option.get("id").asText();
                String value = option.get("value").asText();
                categoryMap.put(id, value);
            }
        }

        List<Activity> activities = new ArrayList<>();
        for (JsonNode option : allOptions) {
            if (option.has("optionId")) { 
                String parentCategoryId = option.get("optionId").asText();
                String id = option.get("id").asText();
                String activityValue = option.get("value").asText();
                boolean disabled = option.has("disabled") && option.get("disabled").asBoolean();
                String category = categoryMap.get(parentCategoryId);
                activities.add(new Activity(id, category, activityValue, disabled));
            }
        }
        return activities;
    }
}
