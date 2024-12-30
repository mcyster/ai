package com.extole.admin.weave.scenarios.jsonpath;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.AiAdvisorService;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.advisor.AdvisorBuilder;
import com.cyster.ai.weave.service.conversation.ActiveConversationBuilder;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.tool.SearchTool;
import com.cyster.ai.weave.service.tool.VoidToolAdapter;
import com.cyster.template.StringTemplate;
import com.extole.admin.weave.scenarios.jsonpath.ExtoleEventStreamJsonPathScenario.Parameters;
import com.extole.admin.weave.scenarios.prehandler.ExtoleApiStore;
import com.extole.admin.weave.session.ExtoleSessionContext;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ExtoleEventStreamJsonPathScenario implements Scenario<Parameters, ExtoleSessionContext> {

    private final Advisor<ExtoleSessionContext> advisor;

    ExtoleEventStreamJsonPathScenario(AiAdvisorService aiAdvisorService, ExtoleApiStore extoleStore,
            ExtoleEventStreamEventsGetTool extoleEventStreamEventsGetTool) {

        String instructionsTemplate = """
                You help to write json path queries to filter events in the extole event steam UI.

                The UI supports Json Path as defined by https://www.rfc-editor.org/rfc/rfc9535

                The event steam is a list of EventSteamEventReponse with attributes:
                - String event_id
                - String event_time  // iso8651 date/time
                - String event_stream_id
                - ConsumerEvent event

                Where event is a ConsumerEvent.

                ConsumerEvent has many subtypes
                - InputConsumerEvent
                - StepConsumerEvent
                - ....
                Use the {{searchTool}} tool to understand the attributes on the different types.

                Its important to load some sample events using the {{eventTool}} tool.
                """;

        SearchTool searchTool = extoleStore.createStoreTool();

        Map<String, String> context = new HashMap<>() {
            {
                put("searchTool", searchTool.getName());
                put("eventTool", extoleEventStreamEventsGetTool.getName());
            }
        };

        String instructions = new StringTemplate(instructionsTemplate).render(context);
        AdvisorBuilder<ExtoleSessionContext> builder = aiAdvisorService.getOrCreateAdvisorBuilder(getName());

        builder.setInstructions(instructions);
        builder.withTool(new VoidToolAdapter<>(searchTool, ExtoleSessionContext.class));
        builder.withTool(extoleEventStreamEventsGetTool);

        this.advisor = builder.getOrCreate();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Scenario", "");
    }

    @Override
    public String getDescription() {
        return "Help write json path queries for an Extole Live Event Stream";
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Class<ExtoleSessionContext> getContextClass() {
        return ExtoleSessionContext.class;
    }

    @Override
    public ActiveConversationBuilder<ExtoleSessionContext> createConversationBuilder(Parameters parameters,
            ExtoleSessionContext context) {
        var builder = this.advisor.createConversationBuilder(context);

        if (parameters != null && parameters.eventStreamId != null) {
            builder.addMessage("The eventStreamId is: " + parameters.eventStreamId);
        }

        if (parameters != null && parameters.jsonPathQuery != null) {
            builder.addMessage("The current json path query is: " + parameters.jsonPathQuery);
        }

        return builder;
    }

    public static record Parameters(@JsonProperty(required = true) String eventStreamId,
            @JsonProperty(required = false) String jsonPathQuery) {
        public Parameters(String eventStreamId) {
            this(eventStreamId, null);
        }
    };
}
