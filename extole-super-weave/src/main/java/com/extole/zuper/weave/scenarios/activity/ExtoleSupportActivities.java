package com.extole.zuper.weave.scenarios.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.extole.jira.support.Activity;
import com.extole.jira.support.SupportTicketService;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

@Component
public class ExtoleSupportActivities {

    private static final String ACTIVITY_JSONL_FILE = "/extole/activities.jsonl";
    private final ObjectMapper objectMapper = JsonMapper.builder().enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .build();

    private List<ActivityDetail> activityDetails;
    
    ExtoleSupportActivities(SupportTicketService supportTicketService) {

    	List<ActivityDetail> activityDetails = new ArrayList<>();

    	List<ActivityInfo> activityInfos = loadActivityInfo();
    	    	
    	for(Activity activity: supportTicketService.getActivities()) {
    		
    		ActivityInfo activityInfo = null;
            for (ActivityInfo info : activityInfos) {
    		    if (activity.category().equalsIgnoreCase(info.category()) &&
    		        activity.name().equalsIgnoreCase(info.name())) {
    		        activityInfo = info;
    		        break;
    		    }
            }
            if (activityInfo != null) {
            	activityDetails.add(new ActivityDetail(activity, activityInfo.keywords()));
            }
    	}
    	
    	this.activityDetails = activityDetails;
    }
    
    public List<ActivityDetail> getActivities() {
    	return this.activityDetails;
    }
    
    private List<ActivityInfo> loadActivityInfo() {
        List<ActivityInfo> activities = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(ACTIVITY_JSONL_FILE);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonNode node = objectMapper.readTree(line);
                String category = node.get("category").asText();
                String name = node.get("value").asText();
                // List<String> keywords =
                // Arrays.asList(node.get("keywords").asText().split(",\\s*"));
                activities.add(new ActivityInfo(category, name, ""));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load activities from JSONL file: " + ACTIVITY_JSONL_FILE, e);
        }

        return activities;
    }
    
    record ActivityInfo(String category, String name, String keywords) {};
}
