package com.cyster.app.sage;

import com.cyster.ai.weave.impl.advisor.AdvisorServiceImpl;


import com.cyster.ai.weave.impl.scenario.ScenarioServiceImpl;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.advisor.AdvisorServiceFactory;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.cyster.ai.weave.service.scenario.ScenarioService;
import com.cyster.ai.weave.service.scenario.ScenarioServiceFactory;
import com.cyster.web.developer.advisors.WebsiteService;
import com.cyster.web.site.WebsiteServiceImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.ApplicationContext;

@Configuration
@EnableWebMvc
public class SageAppConfig implements WebMvcConfigurer {
    private Path sites;
    private URI applicationUri;

    public SageAppConfig(ApplicationContext applicationContext, @Value("${AI_HOME}") String aiHome) {
        this.applicationUri = baseUri(applicationContext);
        
        if (aiHome == null || aiHome.isBlank()) {
            throw new IllegalArgumentException("AI_HOME not defined");
        }
        Path directory = Paths.get(aiHome);
        if (!Files.exists(directory)) {
            throw new IllegalArgumentException("AI_HOME (" + aiHome + ") does not exist");
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("AI_HOME (" + aiHome + ") is not a directory");
        } 
        
        this.sites = directory.resolve("sites");
    }
    
    @Bean
    public AdvisorService getAdvisorService(@Value("${OPENAI_API_KEY}") String openAiApiKey) {    
        if (!StringUtils.hasText(openAiApiKey)) {
            throw new IllegalArgumentException("OPENAI_API_KEY not defined");
        }

        return new AdvisorServiceImpl.Factory().createAdvisorService(openAiApiKey);
    }
    
    @Bean
    public ScenarioService getScenarioService(List<ScenarioLoader> scenarioLoaders, List<Scenario<?,?>> scenarios) {
        return new ScenarioServiceImpl.Factory().createScenarioService(scenarioLoaders, scenarios);
        //return loadScenarioService(scenarioLoaders, scenarios)
    }
    
    @Bean 
    public WebsiteService getWebsiteService() {
        return new WebsiteServiceImpl(applicationUri.resolve("/sites"), sites);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourcePath = "file:" + sites.toAbsolutePath().toString() + "/";
        registry.addResourceHandler("/sites/**")
            .addResourceLocations(resourcePath)
            .setCachePeriod(3600)
            .resourceChain(true)
            .addResolver(new PathResourceResolver());
        
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/");
    }
    
    private AdvisorService loadAdvisorService(String openAiApiKey) {
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
        
        return factory.get().createAdvisorService(openAiApiKey);
    }
    
    private ScenarioService loadScenarioService(List<ScenarioLoader> scenarioLoaders, List<Scenario<?,?>> scenarios) {
        var serviceLoader = ServiceLoader.load(ScenarioServiceFactory.class);
        var factory = serviceLoader.findFirst();
        if (factory.isEmpty()) {
            throw new IllegalStateException("No implementation of: " + ScenarioServiceFactory.class.getSimpleName());
        }
        
        return factory.get().createScenarioService(scenarioLoaders, scenarios);
    }
    
    
    private static URI baseUri(ApplicationContext context) {
        Environment environment = context.getEnvironment();

        String protocol = "http";
        if (environment.getProperty("server.protocol") != null) {
            protocol = environment.getProperty("server.protocol");
        }

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

        try {
            return new URI(protocol + "://" + domain + ":" + port + contextPath);
        } catch (URISyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }
}
