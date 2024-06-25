package com.extole.app.jira;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import com.cyster.ai.weave.impl.advisor.AdvisorServiceImpl;
import com.cyster.ai.weave.impl.scenario.ScenarioServiceImpl;
import com.cyster.ai.weave.service.advisor.AdvisorService;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioLoader;
import com.cyster.ai.weave.service.scenario.ScenarioService;
import com.cyster.web.developer.advisors.WebsiteService;
import com.cyster.web.site.WebsiteServiceImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


@Configuration
@EnableWebMvc
public class JiraAppConfig implements WebMvcConfigurer {
    private Path sites;
    private URI applicationUri;
    
    public JiraAppConfig(ApplicationContext applicationContext, @Value("${AI_HOME}") String aiHome) {
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
    }
    
    @Bean 
    public WebsiteService getWebsiteService() {
        return new WebsiteServiceImpl(applicationUri.resolve("/sites"), sites); 
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
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourcePath = "file:" + sites.toAbsolutePath().toString() + "/";
        registry.addResourceHandler("/sites/**")
                .addResourceLocations(resourcePath)
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
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
