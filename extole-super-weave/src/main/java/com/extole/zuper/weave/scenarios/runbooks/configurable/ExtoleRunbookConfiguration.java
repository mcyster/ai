package com.extole.zuper.weave.scenarios.runbooks.configurable;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.extole.zuper.weave.scenarios.runbooks.ExtoleRunbookScenarioLoader;
import com.extole.zuper.weave.scenarios.runbooks.RunbookSuperScenario;
import com.extole.zuper.weave.scenarios.support.ExtoleSupportHelpSuperScenario;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ExtoleRunbookConfiguration implements ExtoleRunbookScenarioLoader {
    private static final Logger logger = LoggerFactory.getLogger(ExtoleRunbookConfiguration.class);

    private ExtoleSupportHelpSuperScenario helpScenario;
    private List<RunbookSuperScenario> runbookScenarios = new ArrayList<>();
    private List<Scenario<?, ?>> scenarios = new ArrayList<>();

    public ExtoleRunbookConfiguration(ExtoleSupportHelpSuperScenario helpScenario, ApplicationContext context) throws IOException, ExtoleRunbookConfigurationException {
        this.helpScenario = helpScenario;
        registerRunbooks(context);
    }

    @Override
    public List<RunbookSuperScenario> getRunbookScenarios() {
        return runbookScenarios;
    }
    
    @Override
    public List<Scenario<?, ?>> getScenarios() {
        return scenarios;
    }

    private void registerRunbooks(ApplicationContext context) throws IOException, ExtoleRunbookConfigurationException {
        if (context instanceof ConfigurableApplicationContext) {
            logger.info("Runbooks - load");

            ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            Resource[] resources = resolver.getResources("classpath:/extole/runbooks/*.yml");

            ObjectMapper mapper = new YAMLMapper();
            
            for (Resource resource : resources) {
                logger.info("Loading Extole Runbook: " + resource.getURI().toString());
                var name = capitalize(removeExtension(resource.getFilename()));
                if (name.isBlank()) {
                    throw new ExtoleRunbookConfigurationException(resource, "Runbook without filename");
                }
                if (!name.matches("[a-zA-Z0-9]+")) {
                    throw new ExtoleRunbookConfigurationException(resource, "Runbook name must only contain alphanumeric characters: " + name);                    
                }
                name = "ExtoleRunbook" + name;
                
                try (InputStream inputStream = resource.getInputStream()) {
                    ExtoleConfigurableRunbookSuperScenario.Configuration configuration;
                    try {
                        configuration = mapper.readValue(inputStream, ExtoleConfigurableRunbookSuperScenario.Configuration.class);
                    } catch(JsonMappingException exception) {
                        logger.error("Failed to load runbook yml: " + resource.getDescription(), exception);
                        continue;
                    }

                    logger.info("Loaded Extole Runbook: " + name);

                    var runbook= new ExtoleConfigurableRunbookSuperScenario(name, configuration, helpScenario);

                    configurableContext.getBeanFactory().registerSingleton(runbook.getName(), runbook);
                    scenarios.add(runbook);
                    runbookScenarios.add(runbook);
                } catch (IOException exception) {
                    logger.error("Failed to load resource as a ExtoleConfigurableTimeRangeReportTool.Configuration from " + resource.getDescription(), exception);
                }
            }
            logger.info("Runbooks - loaded");
        }
    }

    private static String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String snakeCase = input.replaceAll("([a-z])([A-Z])", "$1_$2");
        snakeCase = snakeCase.replace("-", "_");
        String[] words = snakeCase.split("_");

        StringBuilder capitalizedWords = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedWords.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase());
            }
        }

        return capitalizedWords.toString().trim();
    }

    public static String removeExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return filename;
        }

        return filename.substring(0, lastDotIndex);
    }


}

