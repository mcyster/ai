package com.cyster.weave.impl.scenarios.webshot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ScreenshotOne {
    private final String accessKey;

    public ScreenshotOne(@Value("${SCREENSHOTONE_API_KEY}") String accessKey) {
        this.accessKey = accessKey;
    }

    public ScreenshotOneBuilder createBuilder() {
        return new ScreenshotOneBuilder(accessKey);
    }
}
