package com.cyster.app.sage;

import static org.springframework.ai.autoconfigure.openai.OpenAiProperties.CONFIG_PREFIX;

import com.cyster.assistant.service.advisor.AdvisorService;
import com.cyster.assistant.service.advisor.AdvisorServiceFactory;
import com.cyster.assistant.service.scenario.Scenario;
import com.cyster.assistant.service.scenario.ScenarioLoader;
import com.cyster.assistant.service.scenario.ScenarioService;
import com.cyster.assistant.service.scenario.ScenarioServiceFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.ai.autoconfigure.openai.OpenAiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class SageConfig {
    
    @Bean
    public AdvisorService getAdvisorService(OpenAiProperties openAiProperties) {    
        if (!StringUtils.hasText(openAiProperties.getApiKey())) {
            throw new IllegalArgumentException(
                "No Open API key with the property name " + CONFIG_PREFIX + ".api-key");
        }
    
        
        System.out.println("!!!!!!!!!!!!!!!! sage app config 0");
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("");

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                System.out.println("Resource: " + resource);

                // If it's a JAR file, list its entries
                if (resource.getProtocol().equals("jar")) {
                    String path = resource.getPath();
                    String jarPath = path.substring(5, path.indexOf("!")); // strip off the "file:" prefix and everything after the "!"
                    try (JarFile jarFile = new JarFile(jarPath)) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            System.out.println("::" + resource + ":" + entry.getName());
                        }
                    }
                } else {
                    System.out.println("!!! Other");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("!!!!!!!!!!!!!!!! sage app config 1");
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/services/" + AdvisorServiceFactory.class.getName());
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                System.out.println("Found resource: " + url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("!!!!!!!!!!!!!!!! sage app config 2");

        ServiceLoader<AdvisorServiceFactory> serviceLoader = ServiceLoader.load(AdvisorServiceFactory.class);
        serviceLoader.forEach(factory -> {
            System.out.println("!!!!! Found factory: " + factory.getClass().getName());
        });
        
        var factory = serviceLoader.findFirst();
        if (factory.isEmpty()) {
            throw new IllegalStateException("No implementation of: " + AdvisorServiceFactory.class.getSimpleName());
        }
        
        return factory.get().createAdvisorService(openAiProperties.getApiKey());
    }
    
    @Bean
    public ScenarioService getScenarioService(List<ScenarioLoader> scenarioLoaders, List<Scenario<?,?>> scenarios) {
        var serviceLoader = ServiceLoader.load(ScenarioServiceFactory.class);
        var factory = serviceLoader.findFirst();
        if (factory.isEmpty()) {
            throw new IllegalStateException("No implementation of: " + ScenarioServiceFactory.class.getSimpleName());
        }
        
        return factory.get().createScenarioService(scenarioLoaders, scenarios);
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
        .json()
        .featuresToEnable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
    }
    
    @Bean 
    public Jackson2ObjectMapperBuilder objectMapperBuilder(){
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.failOnUnknownProperties(true);
        return builder;
    }
    
}
