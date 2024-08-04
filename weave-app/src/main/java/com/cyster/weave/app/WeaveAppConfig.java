package com.cyster.weave.app;

import com.cyster.web.rest.ResourceHandlerConfig;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.context.ApplicationContext;

@Configuration
@EnableWebMvc
public class WeaveAppConfig implements WebMvcConfigurer {
    private List<ResourceHandlerConfig> resourceHandlerConfigs;
    private List<Converter<?, ?>> converters;
    
    public WeaveAppConfig(ApplicationContext applicationContext,
        List<ResourceHandlerConfig> resourceHandlerConfigs,
        List<Converter<?, ?>> converters) {
        this.resourceHandlerConfigs = resourceHandlerConfigs;
        this.converters = converters;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (this.resourceHandlerConfigs == null || resourceHandlerConfigs.size() == 0) {
            throw new RuntimeException("Error no ResourceHandlerConfigs, expected at least one");
        }
        
        for(var config: resourceHandlerConfigs) {
            config.addTo(registry);
        }
  
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        for(var converter: converters) {
            registry.addConverter(converter);
        }
    }
}
