package com.extole.zuper.weave.scenarios.activity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.DocumentStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

@Component
public class ExtoleSupportActivityDocuments {
    private static final String ACTIVITY_CSV_FILE = "/extole/activities.csv";
    private static final String CSV_CATEGORY = "category";
    private static final String CSV_ACTIVITY_NAME = "activityName";
    private static final String CSV_KEYWORDS = "keywords";

    private DocumentStore documents;

    public ExtoleSupportActivityDocuments(AiService aiService, AiAdvisorService aiAdvisorService) {
        ObjectMapper objectMapper = new ObjectMapper();

        var documentStoreBuilder = aiService.simpleDocumentStoreBuilder();

        for (var activity : loadActivities()) {
            String json;
            try {
                json = objectMapper.writeValueAsString(activity);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to convert activity to json");
            }
            documentStoreBuilder.addDocument(activity.activityName() + ".json", json);
        }

        this.documents = documentStoreBuilder.create();

    }

    public DocumentStore getDocuments() {
        return this.documents;
    }

    private List<Activity> loadActivities() {
        List<Activity> activities = new ArrayList<>();

        ClassPathResource resource = new ClassPathResource(ACTIVITY_CSV_FILE);
        try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
                CSVReader reader = new CSVReader(inputStreamReader)) {

            List<String[]> lines = reader.readAll();

            String[] headers = lines.get(0);
            HashMap<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerMap.put(headers[i], i);
            }
            if (!headerMap.containsKey(CSV_CATEGORY)) {
                throw new RuntimeException(
                        "Error: File " + ACTIVITY_CSV_FILE + " does not have a column: " + CSV_CATEGORY);
            }
            if (!headerMap.containsKey(CSV_ACTIVITY_NAME)) {
                throw new RuntimeException(
                        "Error: File " + ACTIVITY_CSV_FILE + " does not have a column: " + CSV_ACTIVITY_NAME);
            }
            if (!headerMap.containsKey(CSV_KEYWORDS)) {
                throw new RuntimeException(
                        "Error: File " + ACTIVITY_CSV_FILE + " does not have a column: " + CSV_KEYWORDS);
            }

            for (int i = 1; i < lines.size(); i++) {
                String[] line = lines.get(i);
                String activityName = line[headerMap.get(CSV_ACTIVITY_NAME)];
                String category = line[headerMap.get(CSV_CATEGORY)];
                List<String> keywords = Arrays.asList(line[headerMap.get(CSV_KEYWORDS)].split(" "));

                activities.add(new Activity(category, activityName, keywords));
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return activities;
    }

}
