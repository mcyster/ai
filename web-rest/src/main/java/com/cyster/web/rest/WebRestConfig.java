package com.cyster.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.cyster.web.weave.scenarios.WebsiteService;

@Configuration
public class WebRestConfig {
    private Path sites;
    private URI applicationUri;

    public WebRestConfig(ApplicationContext applicationContext,
        @Value("${AI_HOME}") String aiHome,
        @Value("${app.url:}") String appUrl) {

        if (appUrl != null && !appUrl.isBlank()) {
            try {
                this.applicationUri = new URI(appUrl);
            } catch (URISyntaxException exception) {
                throw new RuntimeException("Configured app.url is invalid", exception);
            }
        } else {
            this.applicationUri = baseUri(applicationContext);
        }

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
    public WebsiteService getWebsiteService() {
        return new WebsiteServiceImpl(applicationUri.resolve("/sites"), sites);
    }

    @Bean
    public ResourceHandlerConfig getSiteResourceHandler() {
        return new ResourceHandlerConfig(this.sites);
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
