package com.cyster.rest.controllers.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.Map;

@Component
public class ConfigController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private ConfigurableEnvironment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void logAllEnvironmentVariables() {
        logger.info("Logging all environment properties:");

        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource.getSource() instanceof Map) {
                ((Map<?, ?>) propertySource.getSource()).forEach((key, value) -> {
                    logger.info("Property: {} = {}", key, value);
                });
            }
        }
    }
}
