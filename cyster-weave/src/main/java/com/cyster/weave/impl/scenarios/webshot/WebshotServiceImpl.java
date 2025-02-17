package com.cyster.weave.impl.scenarios.webshot;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;

@Component
public class WebshotServiceImpl implements WebshotService {
    private static final Logger logger = LoggerFactory.getLogger(WebshotServiceImpl.class);
    private final List<WebshotProvider> webshotProviders;
    private final WebshotService defaultWebshotService;

    // TODO default to a Selenium Webshot service
    public WebshotServiceImpl(List<WebshotProvider> webshotProviders,
            @Qualifier("screenshotOneWebshot") WebshotService webshotService) {
        this.webshotProviders = webshotProviders;
        this.defaultWebshotService = webshotService;
    }

    @Override
    public AccessibleAsset takeSnapshot(String name, String url) {

        logger.info("screenshot {} to {}", url, name);

        for (WebshotProvider provider : webshotProviders) {
            if (provider.canHandle(url)) {
                logger.info("screenshot provider: {}", provider.getClass().getSimpleName());

                return provider.takeSnapshot(name, url);
            }
        }

        logger.info("screenshot provider: default");

        return defaultWebshotService.takeSnapshot(name, url);
    }

}
