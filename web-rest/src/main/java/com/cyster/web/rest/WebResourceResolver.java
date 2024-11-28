package com.cyster.web.rest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import java.util.Arrays;
import java.util.List;

public class WebResourceResolver extends PathResourceResolver {

    private final List<String> allowedExtensions = Arrays.asList("html", "css", "js", "png", "jpg", "ts");

    @Override
    protected Resource resolveResourceInternal(
            HttpServletRequest request,
            String requestPath,
            List<? extends Resource> locations,
            ResourceResolverChain chain) {

        Resource resource = super.resolveResourceInternal(request, requestPath, locations, chain);
                
        if (resource != null && resource.exists()) {
            String filename = resource.getFilename();
            if (filename != null && allowedExtensions.stream().anyMatch(extension -> filename.toLowerCase().endsWith("." + extension))) {
                return resource;
            } else {
                return null; 
            }
        }
        return null;
    }
}
