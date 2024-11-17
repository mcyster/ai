package com.cyster.weave.impl.scenarios.brand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class BrandEnabledCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(BrandEnabledCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String apiKey = context.getEnvironment().getProperty("URL2PNG_API_KEY");
        boolean isApiKeyPresent = apiKey != null && !apiKey.isEmpty();

        if (!isApiKeyPresent) {
            logger.warn("Brand screnario not enabled: BRANDFETCH_API_KEY is not set or empty.");
        }

        return isApiKeyPresent;
    }
}
