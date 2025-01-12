package com.extole.zuper.weave.scenarios.runbooks;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.DocumentStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ExtoleRunbookDocuments {

    private DocumentStore documentStore;

    public ExtoleRunbookDocuments(AiService aiService, AiAdvisorService aiAdvisorService,
            List<RunbookSuperScenario> runbookScenarios, ExtoleRunbookScenarioLoader runbookScenarioLoader,
            ExtoleRunbookDefault defaultRunbook) {
        ObjectMapper objectMapper = new ObjectMapper();

        var documentStoreBuilder = aiService.simpleDocumentStoreBuilder();

        List<RunbookSuperScenario> scenarios = new ArrayList<>();
        scenarios.addAll(runbookScenarios);
        scenarios.addAll(runbookScenarioLoader.getRunbookScenarios());

        for (var runbook : scenarios) {
            var book = new Runbook(runbook.getName(), runbook.getDescription(), runbook.getKeywords());
            String json;
            try {
                json = objectMapper.writeValueAsString(book);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to convert runbook to json");
            }

            documentStoreBuilder.addDocument(runbook.getName() + ".json", json);
        }

        this.documentStore = documentStoreBuilder.create();
    }

    public DocumentStore getDocumentStore() {
        return this.documentStore;
    }

    public static class Runbook {
        private String name;
        private String description;
        private String keywords;

        public Runbook(String name, String description, String keywords) {
            this.name = name;
            this.description = description;
            this.keywords = keywords;
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }

        public String getKeywords() {
            return this.keywords;
        }
    }

}
