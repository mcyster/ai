package com.cyster.weave.impl.scenarios.webshot;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.weave.impl.scenarios.webshot.AssetProvider.Asset;
import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;

import reactor.core.publisher.Mono;

@Component
@Conditional(BrowshotWebshotEnabledCondition.class)
public class BrowshotWebshot implements WebshotService {
    private static final Logger logger = LoggerFactory.getLogger(BrowshotWebshot.class);

    private final String apiKey;
    private final LocalAssetProvider assetProvider;
    private final WebClient webClient;

    public BrowshotWebshot(@Value("${BROWSHOT_API_KEY}") String apiKey, LocalAssetProvider assetProvider) {
        this.apiKey = apiKey;
        this.assetProvider = assetProvider;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)).build();
    }

    @Override
    public AccessibleAsset takeSnapshot(String name, String url) {
        CompletableFuture<AccessibleAsset> accessibleAssetFuture = new CompletableFuture<>();

        logger.info("Requesting screenshot for {}", url);

        createScreenshot(url).flatMap(this::waitForScreenshotReady).flatMap(this::downloadScreenshot).map(content -> {
            Asset asset = assetProvider.putAsset(name, AssetProvider.Type.PNG, content);
            return assetProvider.getAccessibleAsset(asset);
        }).doOnSuccess(accessibleAssetFuture::complete).doOnError(accessibleAssetFuture::completeExceptionally)
                .subscribe();

        return accessibleAssetFuture.join();
    }

    private Mono<Integer> createScreenshot(String url) {
        return webClient.get().uri(uriBuilder -> {
            uriBuilder.scheme("https").host("api.browshot.com").path("/api/v1/screenshot/create").queryParam("url", url)
                    .queryParam("instance_id", "65").queryParam("key", apiKey);
            URI uri = uriBuilder.build();
            return uri;
        }).retrieve().bodyToMono(Map.class).flatMap(response -> {
            Object screenshotId = response.get("id");
            if (screenshotId instanceof Integer id) {
                return Mono.just(id);
            } else {
                return Mono.error(new RuntimeException("Failed to retrieve id: " + response.toString()));
            }
        });
    }

    private Mono<String> waitForScreenshotReady(Integer screenshotId) {
        return Mono.defer(() -> checkScreenshotStatus(screenshotId))
                .repeatWhenEmpty(repeat -> repeat.delayElements(Duration.ofSeconds(2))).timeout(Duration.ofMinutes(1))
                .onErrorMap(e -> new RuntimeException("Screenshot processing timed out", e));
    }

    private Mono<String> checkScreenshotStatus(Integer screenshotId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.scheme("https").host("api.browshot.com").path("/api/v1/screenshot/info")
                        .queryParam("id", screenshotId).queryParam("key", apiKey).build())
                .retrieve().bodyToMono(Map.class).flatMap(response -> {
                    logger.debug("status check: " + response.toString());

                    Object status = response.get("status");
                    if ("finished".equals(status)) {
                        Object screenshotUrl = response.get("screenshot_url");
                        if (screenshotUrl instanceof String url) {
                            return Mono.just(url);
                        } else {
                            return Mono.error(
                                    new RuntimeException("Failed to retrieve screenshot_url: " + response.toString()));
                        }
                    } else {
                        return Mono.empty();
                    }
                });
    }

    private Mono<ByteArrayInputStream> downloadScreenshot(String screenshotUrl) {
        return webClient.get().uri(screenshotUrl).accept(MediaType.IMAGE_PNG).retrieve().bodyToMono(byte[].class)
                .map(ByteArrayInputStream::new);
    }
}
