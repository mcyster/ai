package com.extole.sage.advisors.runbooks;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.advisor.SearchTool;
import com.extole.sage.scenarios.runbooks.ExtoleRunbookOther;
import com.extole.sage.scenarios.runbooks.RunbookScenario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ExtoleRunbookToolFactory {
    
    private SearchTool<Void> searchTool;
    
    public ExtoleRunbookToolFactory(AdvisorService advisorService, 
            List<RunbookScenario> runbookScenarios, ExtoleRunbookOther defaultRunbook) {
        ObjectMapper objectMapper = new ObjectMapper();

        
        var documentStoreBuilder = advisorService.simpleDocumentStoreBuilder();
        
        for(var runbook: runbookScenarios) {
            var book = new Runbook(runbook.getName(), runbook.getDescription(), runbook.getKeywords());
            String json;
            try {
                json = objectMapper.writeValueAsString(book);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to convert runbook to json");
            }
            
            documentStoreBuilder.addDocument(runbook.getName() + ".json", json);
        }

        SearchTool.Builder<Void> builder = advisorService.searchToolBuilder();
        builder
            .withName("runbooks")
            .withDocumentStore(documentStoreBuilder.create());
        
        this.searchTool = builder.create();
    }
    
    public SearchTool<Void> getRunbookSearchTool() {
        return this.searchTool;
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



