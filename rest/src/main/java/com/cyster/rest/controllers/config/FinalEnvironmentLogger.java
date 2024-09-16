package com.cyster.rest.controllers.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.Map;

import java.util.HashMap;

@Component 
public class FinalEnvironmentLogger {

    private static final Logger logger = LoggerFactory.getLogger(FinalEnvironmentLogger.class);

    @Autowired
    private Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void logFinalEnvironmentValues() {
    	
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            logger.info("Active profiles: {}", String.join(", ", activeProfiles));
        } else {
            logger.info("No active profiles set.");
        }

        Map<String, String> finalProperties = new HashMap<>();

        for (PropertySource<?> propertySource : ((org.springframework.core.env.AbstractEnvironment) environment).getPropertySources()) {
            if (propertySource.getSource() instanceof Map) {
                Map<?, ?> source = (Map<?, ?>) propertySource.getSource();
                for (Map.Entry<?, ?> entry : source.entrySet()) {
                    String key = String.valueOf(entry.getKey());
                    String value = environment.getProperty(key); // Get the resolved value
                    finalProperties.put(key, value);
                }
            }
        }

        finalProperties.forEach((key, value) -> {
            logger.info("Property: {} = {}", key, value != null ? value : "<unresolved>");
        });
    }
}