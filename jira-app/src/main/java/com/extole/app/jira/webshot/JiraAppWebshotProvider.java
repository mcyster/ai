package com.extole.app.jira.webshot;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;
import com.cyster.weave.impl.scenarios.webshot.LocalAssetProvider;
import com.cyster.weave.impl.scenarios.webshot.ScreenshotOneBuilder;
import com.cyster.weave.impl.scenarios.webshot.ScreenshotOneWebshot;
import com.cyster.weave.impl.scenarios.webshot.WebshotProvider;

public class JiraAppWebshotProvider implements WebshotProvider {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotOneWebshot.class);

    private final String appUrl;
    private final JiraAppTokenService tokenService;
    private final String accessKey;
    private final LocalAssetProvider assetProvider;

    public JiraAppWebshotProvider(@Value("${app.url}") String appUrl, JiraAppTokenService tokenService,
            @Value("${SCREENSHOTONE_API_KEY}") String accessKey, LocalAssetProvider assetProvider) {
        this.appUrl = appUrl;
        this.tokenService = tokenService;
        this.accessKey = accessKey;
        this.assetProvider = assetProvider;
    }

    public boolean canHandle(String url) {
        logger.info("XXXXXXXXXXXXX url {} appUrl {} ", url, appUrl);

        return url.startsWith(appUrl);
    }

    @Override
    public AccessibleAsset takeSnapshot(String name, String url) {
        var builder = new ScreenshotOneBuilder(accessKey, assetProvider);

        if (url.startsWith(appUrl)) {
            String token = tokenService.getToken();
            logger.info("XXXXXXXXXXXXX token: " + token);

            builder.addHeader("Authorization", "Bearer " + token);
            builder.url(appUrl + "/app-token?url=" + URLEncoder.encode(url, StandardCharsets.UTF_8));
        } else {
            builder.url(url);
        }

        logger.info("Requesting screenshot for {}", url);

        return builder.takeSnapshot(name);
    }

}
