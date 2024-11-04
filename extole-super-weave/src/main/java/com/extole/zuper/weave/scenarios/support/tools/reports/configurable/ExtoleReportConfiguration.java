package com.extole.zuper.weave.scenarios.support.tools.reports.configurable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportAdvisorToolLoader;
import com.extole.zuper.weave.scenarios.support.tools.ExtoleSupportTool;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ExtoleReportConfiguration implements ExtoleSupportAdvisorToolLoader  {
    private static final Logger logger = LogManager.getLogger(ExtoleReportConfiguration.class);

    private ExtoleTrustedWebClientFactory extoleWebClientFactory;
    private List<ExtoleSupportTool<?>> tools = new ArrayList<>();

    public ExtoleReportConfiguration(ExtoleTrustedWebClientFactory extoleWebClientFactory, ApplicationContext context) throws IOException, ExtoleReportConfigurtationException {
        this.extoleWebClientFactory = extoleWebClientFactory;
        registerReports(context);
    }

    @Override
    public List<ExtoleSupportTool<?>> getTools() {
        return tools;
    }

    private void registerReports(ApplicationContext context) throws IOException, ExtoleReportConfigurtationException {
        if (context instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) context;
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            Resource[] resources = resolver.getResources("classpath:/extole/reports/*.yml");

            ObjectMapper mapper = new YAMLMapper();

            for (Resource resource : resources) {
                logger.info("Loading Extole report tool: " + resource.getURI().toString());

                try (InputStream inputStream = resource.getInputStream()) {
                    logger.info("Loading Extole Runbook: " + resource.getURI().toString());
                    var name = capitalize(removeExtension(resource.getFilename()));
                    if (name.isBlank()) {
                        throw new ExtoleReportConfigurtationException("Runbook without filename", resource);
                    }
                    if (!name.matches("[a-zA-Z0-9]+")) {
                        throw new ExtoleReportConfigurtationException("Runbook name must only contain alphanumeric characters: " + name, resource);                    
                    }
                    name = name + "Tool";
                    
                    ExtoleConfigurableTimeRangeReportTool.Configuration configuration;
                    try {
                        configuration = mapper.readValue(inputStream, ExtoleConfigurableTimeRangeReportTool.Configuration.class);
                    } catch(JsonMappingException exception) {
                        logger.error("Failed to load report yml: " + resource.getDescription(), exception);
                        continue;
                    }

                    logger.info("Loaded Extole report tool: " + name);

                    var reportTool = new ExtoleConfigurableTimeRangeReportTool(name, configuration, extoleWebClientFactory);
                    configurableContext.getBeanFactory().registerSingleton(reportTool.getName(), reportTool);

                    tools.add(reportTool);
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

