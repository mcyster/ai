package com.extole.sage.advisors.runbooks;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import com.cyster.store.SimpleVectorStoreService;
import com.extole.sage.scenarios.runbooks.ExtoleRunbookOther;
import com.extole.sage.scenarios.runbooks.RunbookScenario;

@Component
public class RunbookStore {
    public static final String VECTOR_STORE_NAME = "runbooks";
    public static final String CHARSET_METADATA = "charset";
    public static final String SOURCE_METADATA = "source";
    
    private VectorStore runbookStore;
    private Runbook defaultRunbook;
    private Map<String, Runbook> runbooks = new HashMap<>();
    
    /*
    private static final List<Runbook> runbooks = new ArrayList<>();
    static {
        runbooks.add(new Runbook("notifification-alert-monitoring", "triage client monitoring"));
        runbooks.add(new Runbook("notifification-alert-technical", "triage technical alert"));

        runbooks.add(new Runbook("notification-traffic-increase", "notification traffic increase automatic change percentage alerts")); - done
        runbooks.add(new Runbook("notification-traffic-decrease", "notification traffic decrease automatic change percentage alerts")); - done
        runbooks.add(new Runbook("notification-webhook", "notification webhook")); - done
        runbooks.add(new Runbook("notification-prehandler", "notification prehandler")); - done        
        runbooks.add(new Runbook("notification-email-render", "notification email render")); - done     
        runbooks.add(new Runbook("notification-other", "notification"));        
        runbooks.add(new Runbook("prehandler", "prehandler"));        
        runbooks.add(new Runbook("investigate-onsite-tags", "investigate tags zone onsite web"));
        runbooks.add(new Runbook("investigate-flow", "investigate flow"));
        runbooks.add(new Runbook("investigate-errors-reward", "investigate errors rewards gitft card"));
        runbooks.add(new Runbook("investigate-errors-dashboard", "investigate errors dashboard data discrepancies"));
        runbooks.add(new Runbook("creative-customization", "creative customization"));        
        runbooks.add(new Runbook("program-create", "program create new"));
        runbooks.add(new Runbook("data-fixup", "fixup data events bot container"));        
        runbooks.add(new Runbook(STORE_VERSION, "ai version test"));        
    }
*/
    
    public RunbookStore(SimpleVectorStoreService vectorStoreService,  List<RunbookScenario> runbookScenarios, ExtoleRunbookOther defaultRunbook) {
        this.runbookStore = vectorStoreService.getRepository(VECTOR_STORE_NAME);
        this.defaultRunbook = new Runbook(defaultRunbook.getName(), defaultRunbook.getDescription(), defaultRunbook.getKeywords());
        
        vectorStoreService.deleteRespository(VECTOR_STORE_NAME);
        this.runbookStore = vectorStoreService.getRepository(VECTOR_STORE_NAME);

        List<Document> documents = new ArrayList<>();
        for(var runbook: runbookScenarios) {
            runbooks.put(runbook.getName(), new Runbook(runbook.getName(), runbook.getDescription(), runbook.getKeywords()));  
            
            var metadata = new HashMap<String, Object>();
            metadata.put(SOURCE_METADATA, runbook.getName());
            metadata.put(CHARSET_METADATA, StandardCharsets.UTF_8.name());
                
            documents.add(new Document(runbook.getKeywords(), metadata));
        }

        this.runbookStore.add(documents);      
    }
    
    public List<Runbook> query(String query) {
        
        SearchRequest searchRequest = SearchRequest.query(query);
        // searchRequest.withSimilarityThreshold(0.3);
                
        var documents = runbookStore.similaritySearch(searchRequest);
        
        if (documents.isEmpty()) {
            return List.of(this.defaultRunbook);
        }
        
        var results = new ArrayList<Runbook>();
        for (var document: documents) {
            results.add(this.runbooks.get(document.getMetadata().get("source").toString()));
        }
        
        return results; 
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



