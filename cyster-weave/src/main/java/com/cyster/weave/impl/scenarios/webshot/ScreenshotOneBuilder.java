package com.cyster.weave.impl.scenarios.webshot;

import java.io.ByteArrayInputStream;
import java.util.Collections;
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
import com.cyster.weave.impl.scenarios.webshot.AssetProvider.AssetWriter;
import com.fasterxml.jackson.databind.ObjectMapper;

// https://screenshotone.com/docs
// https://github.com/screenshotone/jsdk 

public class ScreenshotOneBuilder {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotOneBuilder.class);

    private final WebClient webClient;
    private String url;
    private Map<String, String> headers = new HashMap<>();

    public ScreenshotOneBuilder(@Value("${SCREENSHOTONE_API_KEY}") String accessKey) {
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

    public Asset takeSnapshot(AssetWriter assetWriter) {
        logger.info("capture {} to {}", this.url);

        try {
            List<String> headerList = headers.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.toList());

            ScreenshotRequest request = new ScreenshotRequest(this.url, headerList);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(request);

            logger.info("https://api.screenshotone.com/take post {}", jsonPayload);

            byte[] imageBytes = webClient.post().uri("https://api.screenshotone.com/take").headers(httpHeaders -> {
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                httpHeaders.setAccept(Collections.singletonList(MediaType.IMAGE_PNG));
            }).body(BodyInserters.fromValue(jsonPayload)).retrieve().bodyToMono(byte[].class).block();

            if (imageBytes != null) {
                ByteArrayInputStream content = new ByteArrayInputStream(imageBytes);
                assetWriter.type(AssetProvider.Type.PNG);
                return assetWriter.write(content);
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
