package com.cyster.weave.impl.scenarios.webshot;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.weave.impl.scenarios.webshot.AssetProvider.Asset;
import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;

// https://browshot.com/
// supports specifying headers

// @Component
@Conditional(BrowshotWebshotEnabledCondition.class)
public class BrowshotWebshot implements Webshot {
    private static final Logger logger = LoggerFactory.getLogger(WebshotTool.class);

    private final String apiKey;
    private final TokenService tokenService;
    private final LocalAssetProvider assetProvider;
    private final WebClient webClient;

    public BrowshotWebshot(@Value("${BROWSHOT_API_KEY}") String apiKey, TokenService tokenService,
            LocalAssetProvider assetProvider) {
        this.apiKey = apiKey;
        this.tokenService = tokenService;
        this.assetProvider = assetProvider;
        this.webClient = WebClient.builder().codecs(
                clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Override
    public AccessibleAsset getImage(String name, String url) {
        CompletableFuture<AccessibleAsset> accessibleAssetFuture = new CompletableFuture<>();

        var token = tokenService.getToken(url);
        final String authorizationHeader = token.isPresent() ? "Authorization: Bearer " + token.get() : null;

        try {
            logger.info("Browshot url {}", url);

            webClient.get().uri(uriBuilder -> {
                uriBuilder.scheme("https").host("api.browshot.com").path("/api/v1/simple").queryParam("url", url)
                        .queryParam("instance_id", "65").queryParam("key", apiKey);
                if (authorizationHeader != null) {
                    uriBuilder.queryParam("header", authorizationHeader);
                }
                return uriBuilder.build();
            }).accept(MediaType.IMAGE_PNG).retrieve().bodyToMono(byte[].class).map(ByteArrayInputStream::new)
                    .doOnNext(content -> {
                        Asset asset = assetProvider.putAsset(name, AssetProvider.Type.PNG, content);
                        accessibleAssetFuture.complete(assetProvider.getAccessibleAsset(asset));
                    }).block();
        } catch (

        Exception exception) {
            throw new RuntimeException("Failed to fetch the image", exception);
        }

        return accessibleAssetFuture.join();
    }

}
