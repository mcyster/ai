package com.cyster.web.rest;

import java.nio.file.Path;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.resource.PathResourceResolver;

// TODO generalize to interface

public class ResourceHandlerConfig {
    private Path sites;
    
    public ResourceHandlerConfig(Path sites) {
        this.sites = sites;    
    }
    
    public void addTo(ResourceHandlerRegistry registry) {        
        String resourcePath = "file:" + sites.toAbsolutePath().toString() + "/";

        registry.addResourceHandler("/sites/**")
            .addResourceLocations(resourcePath)
            .setCachePeriod(0)
            .resourceChain(true)
            .addResolver(new PathResourceResolver());
    }
}
