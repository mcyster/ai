package com.cyster.scheduler.impl;


import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.Scenario.ConversationBuilder;
import com.cyster.weave.service.scenariosession.ScenarioSession;
import com.cyster.weave.service.scenariosession.ScenarioSessionStore;
import com.cyster.ai.weave.service.scenario.ScenarioException;
import com.cyster.ai.weave.service.scenario.ScenarioSet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.quartz.JobDataMap;

@Component
public class ScheduledJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledJob.class);

    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;
    private AtomicReference<Optional<ScenarioSet>> lazyScenarioSet;
    private AtomicReference<Optional<ScenarioSessionStore>> lazyScenarioSessionStore;

    
    public ScheduledJob(ObjectMapper objectMapper,  ApplicationContext applicationContext) {
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
        this.lazyScenarioSet = new AtomicReference<>(Optional.empty());
        this.lazyScenarioSessionStore = new AtomicReference<>(Optional.empty());
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        JobDataMap jobData = jobContext.getMergedJobDataMap();

        String scenarioName = jobData.getString(SchedulerTool.JOB_DATA_SCENARIO);
        
        String prompt = null;
        if (jobData.containsKey(SchedulerTool.JOB_DATA_PROMPT)) {
            prompt = jobData.getString(SchedulerTool.JOB_DATA_PROMPT);
        }
        
        ScenarioSet scenarioSet = getScenarioSet();
        Scenario<?,?> scenario;
        try {
            scenario = scenarioSet.getScenario(scenarioName);
        } catch (ScenarioException e) {
            logger.error("Failed to execute scheduled scenario, scenario " + scenarioName + " not found");
            return;
        }
        
        Object parameters = null;
        if (jobData.containsKey(SchedulerTool.JOB_DATA_PARAMETERS)) {
            try {
                parameters = objectMapper.readValue(jobData.getString(SchedulerTool.JOB_DATA_PARAMETERS), scenario.getParameterClass());
            } catch (JsonProcessingException e) {
                logger.error("Failed to execute scheduled scenario " + scenarioName + " bad parameters: " + parameters);
                return;
            }
        }
   
        ScenarioSession<?, ?> scenarioSession = createScenarioSession(scenario, parameters, null);
                
        logger.info("Scheduled scenario session id: " + scenarioSession.getId() + " started");
                
        Conversation conversation = scenarioSession.getConversation();
        if (prompt != null) {
            conversation.addMessage(Type.USER, prompt);
        }
        
        Message message;
        try {
            message = conversation.respond();
        } catch (ConversationException exception) {
            logger.error("Failed to execute scheduled scenario " + scenarioName, exception);
            return;      
        }
        
        logger.info("Scheduled scenario session id: " + scenarioSession.getId() + " response: " + message.getContent());
    }
    
    private ScenarioSet getScenarioSet() {
        return lazyScenarioSet.updateAndGet(currentValue -> {
            if (!currentValue.isPresent()) {
                return Optional.of(applicationContext.getBean(ScenarioSet.class));
            }
            return currentValue;
        }).orElseThrow(() -> new RuntimeException("Unable to initialize scenario set"));
    }
    
    private ScenarioSessionStore scenarioSessionStore() {
        return lazyScenarioSessionStore.updateAndGet(currentValue -> {
            if (!currentValue.isPresent()) {
                return Optional.of(applicationContext.getBean(ScenarioSessionStore.class));
            }
            return currentValue;
        }).orElseThrow(() -> new RuntimeException("Unable to initialize scenario set"));
    }
    
    @SuppressWarnings("unchecked")
    private <PARAMETERS, CONTEXT> ScenarioSession<PARAMETERS, CONTEXT> createScenarioSession(
            Scenario<PARAMETERS, CONTEXT> scenario, Object parameters, Object context) {
        PARAMETERS castParameters = (PARAMETERS)parameters;
        CONTEXT castContext = (CONTEXT)context;

        ConversationBuilder conversationBuilder = scenario.createConversationBuilder(castParameters, castContext);
        Conversation conversation = conversationBuilder.start();
        
        return scenarioSessionStore().addSession(scenario, castParameters, conversation);
    }
}