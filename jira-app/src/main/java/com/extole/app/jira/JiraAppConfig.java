package com.extole.app.jira;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cyster.web.rest.ResourceHandlerConfig;


@Configuration
@EnableWebMvc
public class JiraAppConfig implements WebMvcConfigurer {
    private List<ResourceHandlerConfig> resourceHandlerConfigs;

    public JiraAppConfig(ApplicationContext applicationContext,
        List<ResourceHandlerConfig> resourceHandlerConfigs) {
        this.resourceHandlerConfigs = resourceHandlerConfigs;
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

}
