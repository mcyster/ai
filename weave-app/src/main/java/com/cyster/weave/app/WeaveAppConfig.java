package com.cyster.weave.app;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cyster.web.WebConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebMvc
public class WeaveAppConfig implements WebMvcConfigurer {
    private List<WebConfigurer> webConfigs;
    private List<Converter<?, ?>> converters;
    private final ObjectMapper objectMapper;

    public WeaveAppConfig(ApplicationContext applicationContext, List<WebConfigurer> resourceHandlerConfigs,
            List<Converter<?, ?>> converters, ObjectMapper objectMapper) {
        this.webConfigs = resourceHandlerConfigs;
        this.converters = converters;
        this.objectMapper = objectMapper;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (this.webConfigs == null || webConfigs.size() == 0) {
            throw new RuntimeException("Error no ResourceHandlerConfigs, expected at least one");
        }

        for (var config : webConfigs) {
            config.addResourceHandlers(registry);
        }

        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        for (var config : webConfigs) {
            config.addContentNegotiations(configurer);
        }
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        for (var converter : converters) {
            registry.addConverter(converter);
        }
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
    }
}
