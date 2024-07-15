package com.extole.sage.scenarios.runbooks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.extole.sage.advisors.support.ExtoleSupportAdvisor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ExtoleRunbookConfiguration implements ScenarioLoader {
    private static final Logger logger = LogManager.getLogger(ExtoleRunbookConfiguration.class); 

    private ExtoleSupportAdvisor advisor;
    private List<Scenario<?,?>> scenarios = new ArrayList<>();
    
    public ExtoleRunbookConfiguration(ExtoleSupportAdvisor advisor, ApplicationContext context) throws IOException, ExtoleRunbookConfigurationException {
        this.advisor = advisor;
        registerRunbooks(context);
    }

    @Override
    public List<Scenario<?, ?>> getScenarios() {
        return scenarios;
    }
    
    private void registerRunbooks(ApplicationContext context) throws IOException, ExtoleRunbookConfigurationException {
        if (context instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            
            Resource[] resources = resolver.getResources("classpath:/extole/runbooks/*.yml");
            
            ObjectMapper mapper = new YAMLMapper();
            
            for (Resource resource : resources) {
                logger.info("Loading Extole Runbook: " + resource.getURI().toString());
                  
                try (InputStream inputStream = resource.getInputStream()) {                   
                    var configuration = mapper.readValue(inputStream, ExtoleConfigurableRunbookScenario.Configuration.class);
                    
                    logger.info("Loaded Extole Runbook: " + configuration.getName());
                    
                    var runbook= new  ExtoleConfigurableRunbookScenario(configuration, advisor);
                    
                    configurableContext.getBeanFactory().registerSingleton(runbook.getName(), runbook);
                    scenarios.add(runbook);
                } catch (IOException exception) {
                    logger.error("Failed to load resource as a ExtoleConfigurableTimeRangeReportTool.Configuration from " + resource.getDescription(), exception);
                }
            }
        }
          
    }

}

