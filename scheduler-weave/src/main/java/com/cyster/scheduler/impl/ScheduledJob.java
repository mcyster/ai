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

import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.Scenario.ConversationBuilder;
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

    public ScheduledJob(ObjectMapper objectMapper,  ApplicationContext applicationContext) {
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
        this.lazyScenarioSet = new AtomicReference<>(Optional.empty());
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
        
        ConversationBuilder conversationBuilder = createConversationBuilder(scenario, parameters, null); 
        
        if (prompt != null) {
            conversationBuilder.addMessage(prompt);
        }
                
        logger.info("Scheduled scenario '" + scenarioName + "' conversation started");
        
        Message conversation;
        try {
            conversation = conversationBuilder.start().respond();
        } catch (ConversationException exception) {
            logger.error("Failed to execute scheduled scenario " + scenarioName, exception);
            return;      
        }

        // TODO make available in ScenarioSession, so you can inspect the conversation 
        
        logger.info("Scheduled scenario '" + scenarioName + "' conversation response: " + conversation.getContent());
    }
    
    private ScenarioSet getScenarioSet() {
        return lazyScenarioSet.updateAndGet(currentValue -> {
            if (!currentValue.isPresent()) {
                return Optional.of(applicationContext.getBean(ScenarioSet.class));
            }
            return currentValue;
        }).orElseThrow(() -> new RuntimeException("Unable to initialize scenario set"));
    }
    
    @SuppressWarnings("unchecked")
    private static <PARAMETERS, CONTEXT> ConversationBuilder createConversationBuilder(
            Scenario<PARAMETERS, CONTEXT> scenario, Object parameters, Object context) {
        PARAMETERS castParameters = (PARAMETERS)parameters;
        CONTEXT castContext = (CONTEXT)context;

        return scenario.createConversationBuilder(castParameters, castContext);
    }
    
}
