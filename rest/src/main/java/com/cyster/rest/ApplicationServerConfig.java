package com.cyster.rest;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

@Component
public class ApplicationServerConfig {
    private ApplicationContext context;

    private static final Logger logger = LogManager.getLogger(ApplicationServerConfig.class);

    public ApplicationServerConfig(ApplicationContext context) {
    	logger.debug("Application Servce Context");
    	
        this.context = context;
    }

    public void dumpBeans() {
        String[] beanNames = context.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            logger.info("spring-bean: " + beanName);
        }
    }

    public void dumpEnvironment() {
        Environment environment = context.getEnvironment();

        for (PropertySource<?> propertySource : ((AbstractEnvironment) environment).getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource<?> eps = (EnumerablePropertySource<?>) propertySource;
                for (String key : eps.getPropertyNames()) {
                    Object value = propertySource.getProperty(key);
                    logger.info("environment: " + key + "=" + value.toString());
                }
            }
        }
    }

    public String getDescription() {
        Environment environment = this.context.getEnvironment();

        String applicationName = "app";
        if (environment.getProperty("spring.application.name") != null) {
            applicationName = environment.getProperty("spring.application.name");
        }
        String protocol = "http";
        if (environment.getProperty("server.protocol") != null) {
            protocol = environment.getProperty("server.protocol");
        }

        String applicationUrl;
        if (environment.getProperty("app.url") != null) {
            applicationUrl = environment.getProperty("app.url");
        } else {
            String domain = "localhost";
            if (environment.getProperty("server.domain") != null) {
                domain = environment.getProperty("server.domain");
            }

            String port = "8080";
            if (environment.getProperty("server.port") != null) {
                port = environment.getProperty("server.port");
            }
            String contextPath = "/";
            if (environment.getProperty("server.servlet.context-path") != null) {
                contextPath = environment.getProperty("server.servlet.context-path");
            }
            applicationUrl = protocol + "://" + domain + ":" + port + contextPath;
        }

        return "Application '" + applicationName + "' listening on " + applicationUrl;

    }
}