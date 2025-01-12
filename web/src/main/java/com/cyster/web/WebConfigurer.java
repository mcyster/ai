package com.cyster.web;

import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

public interface WebConfigurer {

    void addResourceHandlers(ResourceHandlerRegistry registry);

    void addContentNegotiations(ContentNegotiationConfigurer configurer);
}
