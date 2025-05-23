package com.cyster.weave.impl.scenarios.webshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ScreenshotOneEnabledCondition implements Condition {
    private static final Logger logger = LoggerFactory.getLogger(Url2PngWebshotEnabledCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String apiKey = context.getEnvironment().getProperty("SCREENSHOTONE_API_KEY");
        boolean isApiKeyPresent = apiKey != null && !apiKey.isEmpty();

        if (!isApiKeyPresent) {
            logger.warn("ScreenshotOne not avaliable: SCREENSHOTONE_API_KEY is not set or empty.");
        }

        return isApiKeyPresent;
    }
}
