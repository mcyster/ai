package com.cyster.weave.rest.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioException;
import com.cyster.ai.weave.service.scenario.ScenarioSet;
import com.cyster.weave.session.service.scenariosession.ScenarioConversation;
import com.cyster.weave.session.service.scenariosession.ScenarioConversationStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;

@RestController
public class ConversationController {
    private static final String PARAMETER_PREFIX = "parameter.";

    private ScenarioConversationStore scenarioConversationStore;
    private ScenarioSet scenarioStore;
    private ObjectMapper objectMapper;
    private List<ScenarioContextFactory<?>> contextFactories;

    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    public ConversationController(ScenarioConversationStore scenarioSessionStore, ScenarioSet scenarioStore,
            List<ScenarioContextFactory<?>> contextFactories) {
        this.scenarioConversationStore = scenarioSessionStore;
        this.scenarioStore = scenarioStore;
        this.contextFactories = contextFactories;
        this.objectMapper = new ObjectMapper();
        // objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES,
        // true); // doesn't respect required = false annotation
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> index(@RequestParam Map<String, String> allParameters,
            @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level) {

        var scenarioConverstationQueryBuilder = scenarioConversationStore.createQueryBuilder();

        allParameters.entrySet().stream().filter(entry -> entry.getKey().startsWith(PARAMETER_PREFIX))
                .forEach(entry -> {
                    String parameterName = entry.getKey().substring(PARAMETER_PREFIX.length());
                    scenarioConverstationQueryBuilder.withFilterParameter(parameterName, entry.getValue());
                });

        List<ScenarioConversation> conversations = scenarioConverstationQueryBuilder.list();

        return conversations.stream()
                .map(scenarioConversation -> new ConversationResponse.Builder(level).setId(scenarioConversation.id())
                        .setScenario(scenarioConversation.scenarioType().name())
                        .setParameters(scenarioConversation.parameters()).setMessages(scenarioConversation.messages())
                        .build())
                .collect(Collectors.toList());
    }

    @PostMapping("/conversations")
    public ConversationResponse createConversation(
            @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level,
            @RequestHeader MultiValueMap<String, String> headers, @RequestBody ConversationRequest request)
            throws ScenarioNameNotSpecifiedRestException, ScenarioNameNotFoundRestException,
            ScenarioParametersException, ScenarioContextException {

        if (request == null || request.scenario() == null || request.scenario().isBlank()) {
            throw new ScenarioNameNotSpecifiedRestException();
        }

        Scenario<?, ?> scenario;
        try {
            scenario = this.scenarioStore.getScenario(request.scenario());
        } catch (ScenarioException exception) {
            throw new ScenarioNameNotFoundRestException(request.scenario());
        }

        var conversation = createScenarioConversation(scenario, request.parameters(), headers);

        return new ConversationResponse.Builder(level).setId(conversation.id()).setScenario(scenario.getName())
                .setParameters(conversation.parameters()).setMessages(conversation.messages()).build();
    }

    @GetMapping("/conversations/{id}")
    public ConversationResponse getConversation(@PathVariable("id") String id,
            @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level)
            throws ScenarioSessionNotFoundRestException, ScenarioSessionNotSpecifiedRestException {

        if (id == null || id.isBlank()) {
            throw new ScenarioSessionNotSpecifiedRestException();
        }
        Optional<ScenarioConversation> conversation = this.scenarioConversationStore.getSession(id);
        if (conversation.isEmpty()) {
            throw new ScenarioSessionNotFoundRestException(id);
        }

        return new ConversationResponse.Builder(level).setId(conversation.get().id())
                .setScenario(conversation.get().scenarioType().name()).setParameters(conversation.get().parameters())
                .setMessages(conversation.get().messages()).build();
    }

    @PostMapping("/conversations/messages")
    public ConvenienceMessageResponse startConversation(
            @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level,
            @RequestHeader MultiValueMap<String, String> headers, @RequestBody PromptedConversationRequest request)
            throws ScenarioNameNotSpecifiedRestException, ScenarioNameNotFoundRestException, ConversationRestException,
            ScenarioParametersException, ScenarioContextException {

        if (request == null || request.scenario().isBlank()) {
            throw new ScenarioNameNotSpecifiedRestException();
        }

        Scenario<?, ?> scenario;
        try {
            scenario = this.scenarioStore.getScenario(request.scenario());
        } catch (ScenarioException exception) {
            throw new ScenarioNameNotFoundRestException(request.scenario());
        }

        var conversation = createScenarioConversation(scenario, request.parameters(), headers);

        Message answer;
        try {
            if (request.prompt() != null && !request.prompt().isBlank()) {
                conversation.addMessage(Type.USER, request.prompt());
                answer = conversation.respond();
            } else {
                answer = conversation.respond();
            }
        } catch (ConversationException exception) {
            throw new ConversationRestException(conversation.id(), exception);
        }

        MessageResponse messageResponse = new MessageResponse.Builder(level).create(answer.getType().toString(),
                answer.getContent(), answer.operation());

        return new ConvenienceMessageResponse(conversation.id(), messageResponse);
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageResponse> getConversationMessages(@PathVariable("id") String id,
            @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level)
            throws ScenarioSessionNotFoundRestException, ScenarioSessionNotSpecifiedRestException {

        if (id == null || id.isBlank()) {
            throw new ScenarioSessionNotSpecifiedRestException();
        }
        Optional<ScenarioConversation> conversation = this.scenarioConversationStore.getSession(id);
        if (conversation.isEmpty()) {
            throw new ScenarioSessionNotFoundRestException(id);
        }

        var builder = new MessageResponse.Builder(level);
        var messages = new ArrayList<MessageResponse>();
        for (var message : conversation.get().messages()) {
            messages.add(builder.create(message.getType().toString(), message.getContent(), message.operation()));
        }

        return messages;
    }

    @PostMapping("/conversations/{id}/messages")
    public MessageResponse continueConversation(@PathVariable("id") String id,
            @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level,
            @RequestBody MessagePromptRequest request) throws ScenarioSessionNotFoundRestException,
            ScenarioSessionNotSpecifiedRestException, ConversationRestException {

        if (id == null || id.isBlank()) {
            throw new ScenarioSessionNotSpecifiedRestException();
        }
        Optional<ScenarioConversation> conversation = this.scenarioConversationStore.getSession(id);
        if (conversation.isEmpty()) {
            throw new ScenarioSessionNotFoundRestException(id);
        }

        logger.info("Converstation.continue conversationId: " + conversation.get().id());

        Message response;
        try {
            conversation.get().addMessage(Type.USER, request.getPrompt());
            response = conversation.get().respond();
        } catch (ConversationException exception) {
            throw new ConversationRestException(conversation.get().id(), exception);
        }

        return new MessageResponse.Builder(level).create(response.getType().toString(), response.getContent(),
                response.operation());
    }

    @SuppressWarnings("unchecked")
    private <PARAMETERS, CONTEXT> ScenarioConversation createScenarioConversation(
            Scenario<PARAMETERS, CONTEXT> scenario, Map<String, Object> parameterMap,
            MultiValueMap<String, String> headers) throws ScenarioParametersException, ScenarioContextException {

        JsonNode parameterNode = objectMapper.valueToTree(parameterMap);
        PARAMETERS parameters;
        try {
            parameters = objectMapper.treeToValue(parameterNode, scenario.getParameterClass());
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(objectMapper);
            String schema;
            try {
                var schemaJson = schemaGenerator.generateSchema(scenario.getParameterClass());
                schema = objectMapper.writeValueAsString(schemaJson);
            } catch (JsonProcessingException exception1) {
                schema = "Unable to determine schema of " + scenario.getParameterClass().getSimpleName();
            }
            throw new ScenarioParametersException("Parameters do not have the expected attributes: " + schema,
                    exception);
        }

        boolean match = false;
        CONTEXT context = null;
        for (ScenarioContextFactory<?> factory : contextFactories) {
            if (factory.getContextClass().equals(scenario.getContextClass())) {
                context = (CONTEXT) factory.createContext(headers);
                match = true;
            }
        }

        if (!match) {
            throw new ScenarioContextException("No Context Factory Found for Scenario: " + scenario.getName()
                    + " with context type: " + scenario.getContextClass().getName());
        }

        ActiveConversation scenarioConversation = scenario.createConversationBuilder(parameters, context).start();

        return scenarioConversationStore.addConversation(scenarioConversation, scenario.toScenarioType(), parameters,
                context);
    }

}
