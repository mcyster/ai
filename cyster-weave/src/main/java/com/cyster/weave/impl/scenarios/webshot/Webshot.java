package com.cyster.weave.impl.scenarios.webshot;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

// https://www.url2png.com/
// TBD If url requires login / session cookie

@Component
@Conditional(WebshotEnabledCondition.class)
public class Webshot {
    private static final Logger logger = LoggerFactory.getLogger(WebshotTool.class);

    private final String secret;
    private final String apiKey;
    private final LocalAssetProvider assetProvider;
    private final WebClient webClient;

    public Webshot(@Value("${URL2PNG_API_KEY}") String apiKey, @Value("${URL2PNG_SECRET}") String secret,
            LocalAssetProvider assetProvider) {
        this.apiKey = apiKey;
        this.secret = secret;
        this.assetProvider = assetProvider;
        this.webClient = WebClient.builder().codecs(
                clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public AccessibleAsset getImage(String name, String url) {
        String parameterTemplate = "?url=%s&fullpage=true&say_cheese=yes";

        CompletableFuture<AccessibleAsset> accessibleAssetFuture = new CompletableFuture<>();

        try {
            String encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.name());
            String parameters = String.format(parameterTemplate, encodedUrl);

            String rawToken = parameters + this.secret;
            String token = md5Hash(rawToken);

            String requestUrl = String.format("https://api.url2png.com/v6/%s/%s/png/%s", apiKey, token, parameters);

            logger.info("Url2Png requestUrl {}", requestUrl);

            webClient.get().uri(requestUrl).accept(MediaType.IMAGE_PNG).retrieve().bodyToMono(byte[].class)
                    .map(ByteArrayInputStream::new).doOnNext(content -> {
                        Asset asset = assetProvider.putAsset(name, AssetProvider.Type.PNG, content);
                        accessibleAssetFuture.complete(assetProvider.getAccessibleAsset(asset));
                    }).block();
        } catch (Exception exception) {
            throw new RuntimeException("Failed to fetch the image", exception);
        }

        return accessibleAssetFuture.join();
    }

    private String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
