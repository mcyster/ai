package com.extole.weave.scenarios.runbooks.configurable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.extole.weave.scenarios.runbooks.ExtoleRunbookScenarioLoader;
import com.extole.weave.scenarios.runbooks.RunbookScenario;
import com.extole.weave.scenarios.support.ExtoleSupportHelpScenario;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ExtoleRunbookConfiguration implements ExtoleRunbookScenarioLoader {
    private static final Logger logger = LogManager.getLogger(ExtoleRunbookConfiguration.class);

    private ExtoleSupportHelpScenario helpScenario;
    private List<RunbookScenario> runbookScenarios = new ArrayList<>();
    private List<Scenario<?, ?>> scenarios = new ArrayList<>();

    public ExtoleRunbookConfiguration(ExtoleSupportHelpScenario helpScenario, ApplicationContext context) throws IOException, ExtoleRunbookConfigurationException {
        this.helpScenario = helpScenario;
        registerRunbooks(context);
    }

    @Override
    public List<RunbookScenario> getRunbookScenarios() {
        return runbookScenarios;
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
                var name = capitalize(removeExtension(resource.getFilename()));
                if (name.isBlank()) {
                    throw new ExtoleRunbookConfigurationException(resource, "Runbook without filename");
                }
                if (!name.matches("[a-zA-Z0-9]+")) {
                    throw new ExtoleRunbookConfigurationException(resource, "Runbook name must only contain alphanumeric characters: " + name);                    
                }
                name = "extoleRunbook" + name;
                
                try (InputStream inputStream = resource.getInputStream()) {
                    var configuration = mapper.readValue(inputStream, ExtoleConfigurableRunbookScenario.Configuration.class);

                    logger.info("Loaded Extole Runbook: " + name);

                    var runbook= new ExtoleConfigurableRunbookScenario(name, configuration, helpScenario);

                    configurableContext.getBeanFactory().registerSingleton(runbook.getName(), runbook);
                    scenarios.add(runbook);
                    runbookScenarios.add(runbook);
                } catch (IOException exception) {
                    logger.error("Failed to load resource as a ExtoleConfigurableTimeRangeReportTool.Configuration from " + resource.getDescription(), exception);
                }
            }
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

