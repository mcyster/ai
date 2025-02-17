package com.cyster.weave.impl.scenarios.webshot;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.weave.impl.scenarios.webshot.AssetProvider.Asset;
import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;
import com.fasterxml.jackson.databind.ObjectMapper;

// https://screenshotone.com/docs
// 

public class ScreenshotOneBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotOneBuilder.class);

    private final WebClient webClient;
    private final LocalAssetProvider assetProvider;
    private String url;
    private Map<String, String> headers = new HashMap<>();

    public ScreenshotOneBuilder(@Value("${SCREENSHOTONE_API_KEY}") String accessKey, LocalAssetProvider assetProvider) {
        this.assetProvider = assetProvider;
        this.webClient = WebClient.builder().defaultHeader("X-Access-Key", accessKey).codecs(
                clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public ScreenshotOneBuilder url(String url) {
        this.url = url;
        return this;
    }

    public ScreenshotOneBuilder headers(Map<String, String> headers) {
        this.headers = new HashMap<>(headers);
        return this;
    }

    public ScreenshotOneBuilder addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public AccessibleAsset getImage(String name) {
        logger.info("capture {} to {}", this.url, name);

        try {
            List<String> headerList = headers.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.toList());

            ScreenshotRequest request = new ScreenshotRequest(this.url, headerList);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(request);

            byte[] imageBytes = webClient.post().uri("https://api.screenshotone.com/take").headers(httpHeaders -> {
                headers.forEach(httpHeaders::add);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            }).body(BodyInserters.fromValue(jsonPayload)).retrieve().bodyToMono(byte[].class).block();

            if (imageBytes != null) {
                ByteArrayInputStream content = new ByteArrayInputStream(imageBytes);
                Asset asset = assetProvider.putAsset(name, AssetProvider.Type.PNG, content);
                return assetProvider.getAccessibleAsset(asset);
            } else {
                throw new RuntimeException("Failed to fetch the image: Response body is null");
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to fetch the image", exception);
        }
    }

    public record ScreenshotRequest(String url, List<String> headers) {
    }

}
