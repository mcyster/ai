package com.extole.sage.scenarios.graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.cyster.ai.weave.service.advisor.FatalToolException;
import com.cyster.ai.weave.service.advisor.ToolException;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.sage.advisors.support.ExtoleWebClientFactory;
import com.extole.sage.advisors.web.WebAdvisor;
import com.extole.sage.advisors.web.WebsiteService;
import com.extole.sage.advisors.web.WebsiteService.Website;
import com.extole.sage.scenarios.graph.GraphScenario.Parameters;


@Component
public class GraphScenario implements Scenario<Parameters, Void> {
    private static final String NAME = "extoleGraph";
    private WebAdvisor advisor;
    private ExtoleWebClientFactory extoleWebClientFactory;
    private WebsiteService websiteService;

    GraphScenario(WebAdvisor advisor, WebsiteService websiteService, ExtoleWebClientFactory extoleWebClientFactory) {
        this.advisor = advisor;
        this.websiteService = websiteService;
        this.extoleWebClientFactory = extoleWebClientFactory;
        
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Build a webpage to graphical represent a report";
    }
    
    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Parameters parameters, Void context) {
        
        InputStream stream = GraphScenario.class.getResourceAsStream("/extole/web/graph/simple/index.html");
        if (stream == null) {
            throw new RuntimeException("Error unable to load resource:/extole/web/graph/simple/index.html");
        }
        byte[] bytes;
        try {
            bytes = stream.readAllBytes();
        } catch (IOException exception) {
            throw new RuntimeException("Error unable to read resource: /extole/web/graph/simple/index.html", exception);
        }
        String index = new String(bytes, StandardCharsets.UTF_8);
        
        JsonNode report;
        try {
            report = downloadReport(parameters.reportId);
        } catch (ToolException exception) {
           throw new RuntimeException(exception);
        }
        
        String data = reportAsJavascriptFunction(report);
        
        Website website = this.websiteService.create();
                
        website
            .putAsset("index.html", index)
            .putAsset("data.js",  data.replace("```", "&#96;&#96;&#96;"));
        
        String instructions = """ 
The web page at %s is supported by the following assets.

index.html:
```
%s
```

data.js that starts like this:
```
%s
```

Use the file tool to put a script.js file that shows the data.
Tell the user the Url of the web page.  
Then ask the user how they would like to see the data, and update the script to reflext their requests.
""";
        
        
        return advisor.createConversation()
            .withContext(website)
            .setOverrideInstructions(String.format(instructions,
                    website.getUri().toString(),
                    index.replace("```", "&#96;&#96;&#96;"), 
                    getFirstFewLines(data).replace("```", "&#96;&#96;&#96;")))
            .start();
    }

    private JsonNode downloadReport(String reportId) throws ToolException {
        JsonNode result;

        try {
            result = this.extoleWebClientFactory.getSuperUserWebClient()
                // todo limit size of response
                .get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/v4/reports/" + reportId + "/download.json")
                        .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException.Forbidden exception) {
            var errorResponse = exception.getResponseBodyAs(JsonNode.class);
            if (errorResponse.has("code") && errorResponse.path("code").asText().equals("report_not_found")) {
                throw new ToolException("Report with id: " + reportId + " - not found");
            }
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            throw new ToolException("Internal error, unable to get report");
        }
 
        return result;
    }
    
    public static String reportAsJavascriptFunction(JsonNode report) {
        return "function getReport() { return Promise.resolve(" + report.toString() + "); }";
    }
    
    public static String getFirstFewLines(String input) {
        try (BufferedReader reader = new BufferedReader(new StringReader(input))) {
            List<String> lines = reader.lines()
                .limit(40)
                .collect(Collectors.toList());
            return String.join(System.lineSeparator(), lines);
        } catch (Exception exception) {
            throw new RuntimeException("Unable to get first few lines of data", exception);
        }
    }
    public static class Parameters {
        @JsonProperty(required = true)
        public String reportId;
    }
    
}
