package com.extole.weave.scenarios.activity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.SearchTool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;

@Component
public class ExtoleSupportActivityTool {
    private SearchTool<Void> searchTool;

    public ExtoleSupportActivityTool(AiWeaveService aiWeaveService) {
        ObjectMapper objectMapper = new ObjectMapper();

        var documentStoreBuilder = aiWeaveService.simpleDocumentStoreBuilder();
        
        for(var activity: loadActivities()) {
            String json;
            try {
                json = objectMapper.writeValueAsString(activity);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to convert activity to json");
            }
            documentStoreBuilder.addDocument(activity.activityName() + ".json", json);
        }
        
        SearchTool.Builder<Void> builder = aiWeaveService.searchToolBuilder();
        builder
            .withName("activities")
            .withDocumentStore(documentStoreBuilder.create());

        this.searchTool = builder.create();
    }

    public SearchTool<Void> getActivityTool() {
        return this.searchTool;
    }


    private List<Activity> loadActivities() {
        List<Activity> activities = new ArrayList<>();

        try {
            ClassPathResource resource = new ClassPathResource("/extole/activities.csv");
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            CSVReader reader = new CSVReader(inputStreamReader);

            List<String[]> lines = reader.readAll();
            for (String[] line : lines) {
                String name = line[0];
                String[] keywordsArray = line[1].split(",");
                List<String> keywords = new ArrayList<>();
                for (String keyword : keywordsArray) {
                    keywords.add(keyword.trim());
                }
                activities.add(new Activity(name, keywords));
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return activities;
    }
    
}

