package com.cyster.weave.impl.scenarios.webshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;

@Component
@Conditional(ScreenshotOneEnabledCondition.class)
public class ScreenshotOneWebshot implements WebshotService {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotOneWebshot.class);

    private final String accessKey;
    private final LocalAssetProvider assetProvider;

    public ScreenshotOneWebshot(@Value("${SCREENSHOTONE_API_KEY}") String accessKey, LocalAssetProvider assetProvider) {
        this.accessKey = accessKey;
        this.assetProvider = assetProvider;
    }

    @Override
    public AccessibleAsset takeSnapshot(String name, String url) {
        logger.info("takeSnapshot {} to {}", name, url);

        var builder = new ScreenshotOneBuilder(accessKey, assetProvider);
        builder.url(url);

        logger.info("Requesting screenshot for {}", url);

        return builder.takeSnapshot(name);
    }

}
