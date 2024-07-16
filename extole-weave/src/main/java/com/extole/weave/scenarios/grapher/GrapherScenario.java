package com.extole.weave.scenarios.grapher;

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
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.web.developer.advisors.WebAdvisor;
import com.cyster.web.developer.advisors.WebsiteService;
import com.cyster.web.developer.advisors.WebsiteService.Website;
import com.extole.weave.advisors.support.ExtoleWebClientFactory;
import com.extole.weave.scenarios.grapher.GrapherScenario.Parameters;


@Component
public class GrapherScenario implements Scenario<Parameters, Void> {
    private static final String NAME = "extoleGrapher";
    private WebAdvisor advisor;
    private ExtoleWebClientFactory extoleWebClientFactory;
    private WebsiteService websiteService;

    GrapherScenario(WebAdvisor advisor, WebsiteService websiteService, ExtoleWebClientFactory extoleWebClientFactory) {
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

        String indexHtml = loadAsset("/extole/web/graph/simple/index.html");
        String dataJs = loadAsset("/extole/web/graph/simple/data.js");

        JsonNode report;
        try {
            report = downloadReport(parameters.reportId);
        } catch (ToolException exception) {
           throw new RuntimeException(exception);
        }

        String data = report.toPrettyString();

        Website website = this.websiteService.create();

        website.putAsset("index.html", indexHtml);
        website.putAsset("data.js", dataJs);

        String instructions = """
There is a web page at %s?report_id=%s
- don't forget to include the report_id parameter when you mention the url
- we're in developer mode, so localhost is ok

The page is supported by the following assets:

index.html:
```
%s
```

data.js defines a function getData() which returns a Promise.
The promise will provide a data object of the form:
```
%s
```

If there are no explicit instructions, use the file tool to put a script.js file that shows the data.
Tell the user the Url of the web page.
Then ask the user how they would like to see the data, and update script.js to reflect their requests.
""";

        return advisor.createConversation()
            .withContext(website)
            .setOverrideInstructions(String.format(instructions,
                    website.getUri().toString(),
                    parameters.reportId,
                    indexHtml.replace("```", "&#96;&#96;&#96;"),
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

    public static String loadAsset(String assetPath) {
        InputStream stream = GrapherScenario.class.getResourceAsStream(assetPath);
        if (stream == null) {
            throw new RuntimeException("Error unable to load resource:/extole/web/graph/simple/index.html");
        }
        byte[] bytes;
        try {
            bytes = stream.readAllBytes();
        } catch (IOException exception) {
            throw new RuntimeException("Error unable to read resource: /extole/web/graph/simple/index.html", exception);
        }

        return new String(bytes, StandardCharsets.UTF_8);
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
    @Override
    public ConversationBuilder createConversationBuilder(Parameters parameters, Void context) {
        // TODO Auto-generated method stub
        return null;
    }

}
