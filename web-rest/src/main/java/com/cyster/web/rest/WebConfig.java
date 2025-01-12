package com.cyster.web.rest;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import com.cyster.web.WebConfigurer;

import jakarta.servlet.http.HttpServletRequest;

public class WebConfig implements WebConfigurer {
    private Path sites;

    public WebConfig(Path sites) {
        this.sites = sites;
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourcePath = "file:" + sites.toAbsolutePath().toString() + "/";

        registry.addResourceHandler("/sites/**").addResourceLocations(resourcePath).setCachePeriod(0)
                .resourceChain(true).addResolver(new ResourceResolver());
    }

    public void addContentNegotiations(ContentNegotiationConfigurer configurer) {
        configurer.mediaType("ts", org.springframework.http.MediaType.valueOf("application/typescript"));
    }

    public static class ResourceResolver extends PathResourceResolver {

        private final List<String> allowedExtensions = Arrays.asList("html", "css", "js", "png", "jpg", "ts");

        @Override
        protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
                List<? extends Resource> locations, ResourceResolverChain chain) {

            Resource resource = super.resolveResourceInternal(request, requestPath, locations, chain);

            if (resource != null && resource.exists()) {
                String filename = resource.getFilename();
                if (filename != null && allowedExtensions.stream()
                        .anyMatch(extension -> filename.toLowerCase().endsWith("." + extension))) {
                    return resource;
                } else {
                    return null;
                }
            }
            return null;
        }
    }

}
