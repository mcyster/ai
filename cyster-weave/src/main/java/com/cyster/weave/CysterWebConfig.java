package com.cyster.weave;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import com.cyster.weave.impl.scenarios.webshot.LocalAssetProvider;
import com.cyster.web.WebConfigurer;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CysterWebConfig implements WebConfigurer {
    private final LocalAssetProvider localAssetProvider;
    private URI applicationUri;
    private String aiHome;

    public CysterWebConfig(ApplicationContext applicationContext, @Value("${app.url:}") String appUrl,
            @Value("${AI_HOME}") String aiHome) {
        if (appUrl != null && !appUrl.isBlank()) {
            try {
                this.applicationUri = new URI(appUrl);
            } catch (URISyntaxException exception) {
                throw new RuntimeException("Configured app.url is invalid", exception);
            }
        } else {
            this.applicationUri = baseUri(applicationContext);
        }

        this.aiHome = aiHome;
        this.localAssetProvider = new LocalAssetProvider(applicationUri.resolve("/assets/"), aiHome);

    }

    @Bean
    LocalAssetProvider getLocalAssetProvider() {
        return this.localAssetProvider;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourcePath = "file:" + localAssetProvider.localPath().toAbsolutePath().toString() + "/";

        registry.addResourceHandler("/assets/**").addResourceLocations(resourcePath).setCachePeriod(0)
                .resourceChain(true).addResolver(new ResourceResolver());
    }

    public void addContentNegotiations(ContentNegotiationConfigurer configurer) {
    }

    public static class ResourceResolver extends PathResourceResolver {

        private final List<String> allowedExtensions = Arrays.asList("png", "jpg");

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

    private static URI baseUri(ApplicationContext context) {
        Environment environment = context.getEnvironment();

        String protocol = "http";
        if (environment.getProperty("server.protocol") != null) {
            protocol = environment.getProperty("server.protocol");
        }

        String domain = "localhost";
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            domain = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            domain = "localhost";
        }

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
