package com.cyster.weave.impl.scenarios.webshot;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;

@Component
@Conditional(ScreenshotOneEnabledCondition.class)
public class ScreenshotOneWebshot implements Webshot {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotOneWebshot.class);

    private final String accessKey;
    private final TokenService tokenService;
    private final String appUrl;
    private final LocalAssetProvider assetProvider;

    public ScreenshotOneWebshot(@Value("${app.url}") String appUrl, @Value("${SCREENSHOTONE_API_KEY}") String accessKey,
            TokenService tokenService, LocalAssetProvider assetProvider) {
        this.accessKey = accessKey;
        this.tokenService = tokenService;
        this.appUrl = appUrl;
        this.assetProvider = assetProvider;
    }

    @Override
    public AccessibleAsset getImage(String name, String url) {
        logger.info("XXXXXXXXXXXXX get token");

        var builder = new ScreenshotOneBuilder(accessKey, assetProvider);

        var token = tokenService.getToken(url);
        if (token.isPresent()) {
            builder.addHeader("Authorization", "Bearer " + token.get());
            builder.url(appUrl + "/app-token?url=" + URLEncoder.encode(url, StandardCharsets.UTF_8));
        } else {
            builder.url(url);
        }

        logger.info("Requesting screenshot for {}", url);

        return builder.getImage(name);
    }

}
