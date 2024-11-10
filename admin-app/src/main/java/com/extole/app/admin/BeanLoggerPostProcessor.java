package com.extole.app.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;

//@Component
public class BeanLoggerPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BeanLoggerPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        logger.info("before {}", beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        logger.info("after  {}", beanName);
        return bean;
    }
}
