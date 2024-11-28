package com.cyster.web.rest;

import java.nio.file.Path;

import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

public class WebConfig {
    private Path sites;
    
    public WebConfig(Path sites) {
        this.sites = sites;    
    }
    
    public void addResourceHandlers(ResourceHandlerRegistry registry) {        
        String resourcePath = "file:" + sites.toAbsolutePath().toString() + "/";

        registry.addResourceHandler("/sites/**")
            .addResourceLocations(resourcePath)
            .setCachePeriod(0)
            .resourceChain(true)
            .addResolver(new WebResourceResolver());
    }
    
    public void addContentNegotiations(ContentNegotiationConfigurer configurer) {
        configurer.mediaType("ts", org.springframework.http.MediaType.valueOf("application/typescript"));
    }
}
 
