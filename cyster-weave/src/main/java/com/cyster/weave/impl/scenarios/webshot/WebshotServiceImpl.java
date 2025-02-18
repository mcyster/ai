package com.cyster.weave.impl.scenarios.webshot;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cyster.weave.impl.scenarios.webshot.AssetProvider.Asset;
import com.cyster.weave.impl.scenarios.webshot.AssetProvider.AssetWriter;
import com.cyster.weave.impl.scenarios.webshot.AssetProvider.Type;
import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;

@Component
public class WebshotServiceImpl implements WebshotService {
    private static final Logger logger = LoggerFactory.getLogger(WebshotServiceImpl.class);
    private final List<WebshotProvider> webshotProviders;
    private final WebshotProvider defaultWebshotProvider;

    private final LocalAssetProvider localAssetProvider;

    public WebshotServiceImpl(List<WebshotProvider> webshotProviders, DefaultWebshotProvider defaultWebsiteProvider,
            LocalAssetProvider localAssetProvider) {
        this.defaultWebshotProvider = defaultWebsiteProvider;
        this.webshotProviders = webshotProviders.stream()
                .filter(provider -> !(provider instanceof DefaultWebshotProvider)).collect(Collectors.toList());

        this.localAssetProvider = localAssetProvider;
    }

    @Override
    public AccessibleAsset takeSnapshot(String name, String url) {

        logger.info("screenshot {} to {}", url, name);

        WebshotProvider webshotProvider = defaultWebshotProvider;
        for (WebshotProvider provider : webshotProviders) {
            if (provider.canHandle(url)) {
                logger.info("screenshot provider: {}", provider.getClass().getSimpleName());
                webshotProvider = provider;
            }
        }

        AssetWriter assetWriter = localAssetProvider.createAssetWriter(name, Type.PNG);
        Asset asset = webshotProvider.takeSnapshot(assetWriter, url);

        return localAssetProvider.getAccessibleAsset(asset);
    }

}
