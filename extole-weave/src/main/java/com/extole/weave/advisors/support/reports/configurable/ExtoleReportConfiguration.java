package com.extole.weave.advisors.support.reports.configurable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.extole.weave.advisors.support.ExtoleSupportTool;
import com.extole.weave.advisors.support.ExtoleSupportAdvisorToolLoader;
import com.extole.weave.advisors.support.ExtoleWebClientFactory;
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

    private ExtoleWebClientFactory extoleWebClientFactory;
    private List<ExtoleSupportTool<?>> tools = new ArrayList<>();

    public ExtoleReportConfiguration(ExtoleWebClientFactory extoleWebClientFactory, ApplicationContext context) throws IOException, ExtoleReportConfigurtationException {
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
                    var configuration = mapper.readValue(inputStream, ExtoleConfigurableTimeRangeReportTool.Configuration.class);

                    logger.info("Loaded Extole report tool: " + configuration.getName());

                    var reportTool = new ExtoleConfigurableTimeRangeReportTool(configuration, extoleWebClientFactory);
                    configurableContext.getBeanFactory().registerSingleton(reportTool.getName(), reportTool);

                    tools.add(reportTool);
                } catch (IOException exception) {
                    logger.error("Failed to load resource as a ExtoleConfigurableTimeRangeReportTool.Configuration from " + resource.getDescription(), exception);
                }
            }
        }
    }

}

