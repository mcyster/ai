package com.cyster.weave.impl.scenarios.webshot;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.weave.impl.scenarios.webshot.AssetProvider.Asset;
import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;

import reactor.core.publisher.Mono;

// https://browshot.com/
// supports specifying headers

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
        this.webClient = WebClient.builder().codecs(
                clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Override
    public AccessibleAsset getImage(String name, String url) {
        CompletableFuture<AccessibleAsset> accessibleAssetFuture = new CompletableFuture<>();

        var token = tokenService.getToken(url);
        final String authorizationHeader = token.isPresent() ? "Authorization: Bearer " + token.get() : null;

        // Quick fix to pass token to the page, so we can pass it on in Ajax request
        if (token.isPresent()) {
            url = url.contains("?") ? url + "&token=" + token : url + "?token=" + token;
        }

        final String snapshotUrl = url;

        try {
            logger.info("Browshot url {} with key {}", url, apiKey);

            webClient.get().uri(uriBuilder -> {
                uriBuilder.scheme("https").host("api.browshot.com").path("/api/v1/simple")
                        .queryParam("url", snapshotUrl).queryParam("instance_id", "65").queryParam("key", apiKey);
                if (authorizationHeader != null) {
                    uriBuilder.queryParam("header", authorizationHeader);
                }
                return uriBuilder.build();
            }).accept(MediaType.IMAGE_PNG).exchangeToMono(response -> handleResponse(response, name)).block();

        } catch (Exception exception) {
            throw new RuntimeException("Failed to fetch the image", exception);
        }

        return accessibleAssetFuture.join();
    }

    private Mono<AccessibleAsset> handleResponse(ClientResponse response, String name) {
        if (response.statusCode().equals(HttpStatus.FOUND)) {
            logger.info("Redirect");

            return response.headers().asHttpHeaders().getFirst("Location") != null
                    ? followRedirect(response.headers().asHttpHeaders().getFirst("Location"), name)
                    : Mono.error(new RuntimeException("302 redirect received but no Location header found"));
        } else if (response.statusCode().is2xxSuccessful()) {
            logger.info("Downloading");

            return response.bodyToMono(byte[].class).map(ByteArrayInputStream::new).map(content -> {
                Asset asset = assetProvider.putAsset(name, AssetProvider.Type.PNG, content);
                return assetProvider.getAccessibleAsset(asset);
            });
        } else {
            return response.createException().flatMap(Mono::error);
        }
    }

    private Mono<AccessibleAsset> followRedirect(String location, String name) {
        logger.info("Following redirect to: {}", location);
        return webClient.get().uri(URI.create(location)).accept(MediaType.IMAGE_PNG).retrieve().bodyToMono(byte[].class)
                .map(ByteArrayInputStream::new).map(content -> {
                    Asset asset = assetProvider.putAsset(name, AssetProvider.Type.PNG, content);
                    return assetProvider.getAccessibleAsset(asset);
                });
    }
}
