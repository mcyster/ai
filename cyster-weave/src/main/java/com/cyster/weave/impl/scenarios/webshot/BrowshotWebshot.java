package com.cyster.weave.impl.scenarios.webshot;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
public class BrowshotWebshot implements Webshot {
    private static final Logger logger = LoggerFactory.getLogger(BrowshotWebshot.class);

    private final String apiKey;
    private final TokenService tokenService;
    private final LocalAssetProvider assetProvider;
    private final WebClient webClient;

    public BrowshotWebshot(@Value("${BROWSHOT_API_KEY}") String apiKey, TokenService tokenService,
            LocalAssetProvider assetProvider) {
        this.apiKey = apiKey;
        this.tokenService = tokenService;
        this.assetProvider = assetProvider;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)).build();
    }

    @Override
    public AccessibleAsset getImage(String name, String url) {
        CompletableFuture<AccessibleAsset> accessibleAssetFuture = new CompletableFuture<>();

        logger.info("XXXXXXXXXXXXX get token");

        var token = tokenService.getToken(url);
        Map<String, String> headers = new HashMap<>();
        if (token.isPresent()) {
            headers.put("Authorization", "Bearer " + token.get());
        }

        logger.info("Requesting screenshot for {}", url);

        createScreenshot(url, headers).flatMap(this::waitForScreenshotReady).flatMap(this::downloadScreenshot)
                .map(content -> {
                    Asset asset = assetProvider.putAsset(name, AssetProvider.Type.PNG, content);
                    return assetProvider.getAccessibleAsset(asset);
                }).doOnSuccess(accessibleAssetFuture::complete).doOnError(accessibleAssetFuture::completeExceptionally)
                .subscribe();

        return accessibleAssetFuture.join();
    }

    private Mono<Integer> createScreenshot(String url, Map<String, String> headers) {
        Optional<String> headerParameter = headerText(headers);

        return webClient.get().uri(uriBuilder -> {
            uriBuilder.scheme("https").host("api.browshot.com").path("/api/v1/screenshot/create").queryParam("url", url)
                    .queryParam("instance_id", "65").queryParam("key", apiKey);

            if (headerParameter.isPresent()) {
                uriBuilder.queryParam("headers", headerParameter);
            }

            URI uri = uriBuilder.build();
            logger.info("XXXXXXXXXXXXXXXXXXXXXX URI: {}", uri);
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

    private Optional<String> headerText(Map<String, String> headers) {
        if (headers.isEmpty()) {
            return Optional.empty();
        }

        String headerText = headers.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));

        return Optional.of(headerText);
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
