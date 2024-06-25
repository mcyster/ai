package com.cyster.app.sage.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioException;
import com.cyster.ai.weave.service.scenario.ScenarioService;
import com.cyster.sage.service.scenariosession.ScenarioSession;
import com.cyster.sage.service.scenariosession.ScenarioSessionStore;
import com.extole.sage.session.ExtoleSessionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;


@RestController
public class ConversationController {
    private ScenarioSessionStore scenarioSessionStore;
    private ScenarioService scenarioStore;
    private ObjectMapper objectMapper;

    private static final Logger logger = LogManager.getLogger(ConversationController.class);

    public ConversationController(ScenarioSessionStore scenarioSessionStore, ScenarioService scenarioStore) {
        this.scenarioSessionStore = scenarioSessionStore;
        this.scenarioStore = scenarioStore;
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
    }

    @GetMapping("/conversations")
    public List<ConversationResponse> index(
        @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level) {
        return scenarioSessionStore.createQueryBuilder().list().stream()
            .map(value -> new ConversationResponse.Builder(level).setId(value.getId())
                .setMessages(value.getConversation().getMessages()).build())
            .collect(Collectors.toList());
    }

    @PostMapping("/conversations")
    public ConversationResponse create_conversation(
        @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level,
        @RequestHeader MultiValueMap<String, String> headers,
        @RequestBody ConversationRequest request)
        throws ScenarioNameNotSpecifiedRestException, ScenarioNameNotFoundRestException,ScenarioParametersException, ScenarioContextException {
           
        if (request == null || request.scenario() == null || request.scenario().isBlank()) {
            throw new ScenarioNameNotSpecifiedRestException();
        }

        Scenario<?,?> scenario;
        try {
            scenario = this.scenarioStore.getScenario(request.scenario());
        } catch (ScenarioException exception) {
            throw new ScenarioNameNotFoundRestException(request.scenario());
        }
        
        var conversation = createConversation(scenario, request.parameters(), headers);

        var handle = scenarioSessionStore.addSession(scenario, conversation);

        return new ConversationResponse.Builder(level)
            .setId(handle.getId())
            .setScenario(scenario.getName())
            .setMessages(conversation.getMessages())
            .build();
    }

    @PostMapping("/conversations/messages")
    public ConvenienceConversationResponse startConversation(
        @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level,
        @RequestHeader MultiValueMap<String, String> headers,
        @RequestBody PromptedConversationRequest request)
        throws ScenarioNameNotSpecifiedRestException, ScenarioNameNotFoundRestException, ConversationRestException, ScenarioParametersException, ScenarioContextException {

        if (request == null || request.scenario().isBlank()) {
            throw new ScenarioNameNotSpecifiedRestException();
        }

        Scenario<?,?> scenario;
        try {
            scenario = this.scenarioStore.getScenario(request.scenario());
        } catch (ScenarioException exception) {
            throw new ScenarioNameNotFoundRestException(request.scenario());
        }
    
        var conversation = createConversation(scenario, request.parameters(), headers);

        var handle = scenarioSessionStore.addSession(scenario, conversation);

        Message answer;
        try {
            if (request.prompt() != null && !request.prompt().isBlank()) {
                conversation.addMessage(Type.USER, request.prompt());
                answer = conversation.respond();   
            } else {
                answer = conversation.respond();
            }
        } catch (ConversationException exception) {
            throw new ConversationRestException(handle.getId(), exception);
        }

        var response = new ConversationResponse.Builder(level).setId(handle.getId())
            .setScenario(scenario.getName()).setMessages(handle.getConversation().getMessages()).build();

        var conveneinceReponse = new ConvenienceConversationResponse(response, answer.getContent());
        return conveneinceReponse;
    }

    @GetMapping("/conversations/{id}/messages")
    public List<MessageResponse> get_conversation_messages(
        @PathVariable("id") String id,
        @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level)
        throws ScenarioSessionNotFoundRestException, ScenarioSessionNotSpecifiedRestException {

        if (id == null || id.isBlank()) {
            throw new ScenarioSessionNotSpecifiedRestException();
        }
        Optional<ScenarioSession> session = this.scenarioSessionStore.getSession(id);
        if (session.isEmpty()) {
            throw new ScenarioSessionNotFoundRestException(id);
        }

        var builder = new MessageResponse.Builder(level);
        var messages = new ArrayList<MessageResponse>();
        for (var message : session.get().getConversation().getMessages()) {
            messages.add(builder.create(message.getType().toString(), message.getContent(), message.operation()));
        }

        return messages;
    }

    @PostMapping("/conversations/{id}/messages")
    public MessageResponse continue_conversation(
        @PathVariable("id") String id,
        @RequestParam(name = "level", required = false, defaultValue = "Quiet") MessageResponse.Level level,
        @RequestBody MessagePromptRequest request)
        throws ScenarioSessionNotFoundRestException, ScenarioSessionNotSpecifiedRestException,
        ConversationRestException {

        if (id == null || id.isBlank()) {
            throw new ScenarioSessionNotSpecifiedRestException();
        }
        Optional<ScenarioSession> session = this.scenarioSessionStore.getSession(id);
        if (session.isEmpty()) {
            throw new ScenarioSessionNotFoundRestException(id);
        }

        logger.info("Converstation.continue conversationId: " + session.get().getId());

        Message response;
        try {
            session.get().getConversation().addMessage(Type.USER, request.getPrompt());
            response = session.get().getConversation().respond();
        } catch (ConversationException exception) {
            throw new ConversationRestException(session.get().getId(), exception);
        }


        return new MessageResponse.Builder(level)
            .create(response.getType().toString(), response.getContent(), response.operation());
    }

    // TODO make this pluggable
    private ExtoleSessionContext getSessionContext(MultiValueMap<String, String> headers) throws ScenarioContextException {
        if (headers == null || !headers.containsKey("authorization")) {
            throw new ScenarioContextException("Unable to create ExtoleSessionContext expected Authorization header");
        }
        String authorizationHeader = headers.getFirst("authorization");
        
        if (authorizationHeader != null) {
            var accessToken = authorizationHeader.replace("Bearer ", "");
            if (accessToken.length() > 0) {
                return new ExtoleSessionContext(accessToken);
            }
        }
        
        throw new ScenarioContextException("Unable to create ExtoleSessionContext, Authorization header exists but not token found");
    }

    @SuppressWarnings("unchecked")
    private <PARAMETERS, CONTEXT> Conversation createConversation(Scenario<PARAMETERS, CONTEXT> scenario, Map<String, Object> parameterMap, 
        MultiValueMap<String, String> headers) 
        throws ScenarioParametersException, ScenarioContextException {
        
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
            throw new ScenarioParametersException("Parameters do not have the expected attributes: " + schema, exception);
        }

        CONTEXT context = null;
        if (scenario. getContextClass() == ExtoleSessionContext.class) {   
            context = (CONTEXT)getSessionContext(headers);
        }
        
        return scenario.createConversation(parameters, context);  
    }
}
