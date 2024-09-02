package com.cyster.scheduler.impl;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.assistant.OperationLogger;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.scenario.ScenarioSet;
import com.cyster.scheduler.impl.SchedulerTool.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SchedulerTool implements Tool<Parameters, Void> {
    static final String JOB_DATA_SCENARIO = "scenario";
    static final String JOB_DATA_PROMPT = "prompt";
    static final String JOB_DATA_PARAMETERS = "paramters";
    
    private Scheduler scheduler;
    private ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;
    private AtomicReference<Optional<ScenarioSet>> lazyScenarioSet;

    SchedulerTool(Scheduler scheduler, ObjectMapper objectMapper, ApplicationContext applicationContext) {
        this.scheduler = scheduler;
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
        this.lazyScenarioSet = new AtomicReference<>(Optional.empty());
    }
    
    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }
    
    @Override
    public String getDescription() {
        return "Get the json schema of a report given the Extole client id and report id";
    }

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    public Object execute(Parameters parameters, Void context, OperationLogger operation) throws ToolException {
        ScenarioSet scenarioSet = getScenarioSet();
        if (!scenarioSet.hasScenario(parameters.scenario())) {
            throw new ToolException("Scenario not found: " + parameters.scenario());
        }
        
        Duration delay;
        try {
            delay = Duration.parse(parameters.delay());
        } catch (DateTimeParseException e) {
            throw new ToolException("Invalid ISO 8601 duration format: " + parameters.delay());
        }
        
        JobDataMap jobData = new JobDataMap();
        jobData.put(JOB_DATA_SCENARIO, parameters.scenario());
        if (parameters.prompt() != null && !parameters.prompt().isBlank()) {
            jobData.put(JOB_DATA_PROMPT, parameters.prompt());
        }
        
        if (parameters.parameters() != null) {
            try {
                jobData.put(JOB_DATA_PARAMETERS, objectMapper.writeValueAsString(parameters));
            } catch (JsonProcessingException exception) {
                throw new FatalToolException("Unable to convert parameters to json: " + parameters.toString(), exception);
            }
        }

        JobDetail jobDetail = JobBuilder.newJob(ScheduledJob.class)
                .withIdentity("scheduledScenario", "aiWeaveGroup")
                .usingJobData(jobData)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("scenarioTrigger", "aiWeaveGroup")
                .startAt(new Date(System.currentTimeMillis() + delay.toMillis()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException exception) {
            try {
                throw new ToolException("Unable to setup the request schedule for scenario:"  
                    + objectMapper.writeValueAsString(parameters), exception);
            } catch (JsonProcessingException exception2) {
                throw new FatalToolException("Unable to setup the request schedule, internal error", exception2);
            }
        }
        
        return JsonNodeFactory.instance.objectNode();
    }
    
    private ScenarioSet getScenarioSet() {
        return lazyScenarioSet.updateAndGet(currentValue -> {
            if (!currentValue.isPresent()) {
                return Optional.of(applicationContext.getBean(ScenarioSet.class));
            }
            return currentValue;
        }).orElseThrow(() -> new RuntimeException("Unable to initialize scenario set"));
    }
    
    static record Parameters(
        @JsonPropertyDescription("Scenario to execute after the delay") @JsonProperty(required = true) String scenario,
        @JsonPropertyDescription("Prompt for the scenario") @JsonProperty(required = false) String prompt,
        @JsonPropertyDescription("Parameters to pass to the for the scenario") @JsonProperty(required = false) Object parameters,
        @JsonPropertyDescription("Delay an ISO 8601 duration") @JsonProperty(required = true) String delay) {}

}
