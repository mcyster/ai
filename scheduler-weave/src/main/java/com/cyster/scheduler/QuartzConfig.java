package com.cyster.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class QuartzConfig {

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        
        factory.setJobFactory(new SpringBeanJobFactory());
        
        factory.setOverwriteExistingJobs(true);  
        factory.setAutoStartup(true);           
        
        return factory;
    }
}

