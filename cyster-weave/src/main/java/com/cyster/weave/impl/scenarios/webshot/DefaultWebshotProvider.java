package com.cyster.weave.impl.scenarios.webshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.cyster.weave.impl.scenarios.webshot.AssetProvider.Asset;
import com.cyster.weave.impl.scenarios.webshot.AssetProvider.AssetWriter;

@Component
@Conditional(ScreenshotOneEnabledCondition.class)
public class DefaultWebshotProvider implements WebshotProvider {
    private static final Logger logger = LoggerFactory.getLogger(DefaultWebshotProvider.class);

    private final ScreenshotOne screenshotOne;

    public DefaultWebshotProvider(ScreenshotOne screenshotOne) {
        this.screenshotOne = screenshotOne;
    }

    @Override
    public boolean canHandle(String url) {
        return true;
    }

    @Override
    public Asset takeSnapshot(AssetWriter writer, String url) {
        logger.info("takeSnapshot {}", url);

        var builder = screenshotOne.createBuilder();
        builder.url(url);

        logger.info("Requesting screenshot for {}", url);

        return builder.takeSnapshot(writer);
    }

}
