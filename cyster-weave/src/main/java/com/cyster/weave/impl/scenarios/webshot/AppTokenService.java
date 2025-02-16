package com.cyster.weave.impl.scenarios.webshot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class AppTokenService implements TokenProvider {
    private final String baseUri;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final WebClient webClient;

    public AppTokenService(@Value("${app.url}") String baseUri, @Value("${app.token_uri}") String tokenUri,
            @Value("${app.client_id}") String clientId, @Value("${app.client_secret}") String clientSecret,
            @Value("${app.refresh_token}") String refreshToken, WebClient.Builder webClientBuilder) {
        this.baseUri = baseUri;
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;

        this.webClient = webClientBuilder.build();
    }

    @Override
    public String baseUri() {
        return baseUri;
    }

    @Override
    public String getToken() {
        return refreshToken().idToken();
    }

    public TokenResponse refreshToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);
        formData.add("grant_type", "refresh_token");

        return webClient.post().uri(tokenUri).bodyValue(formData).retrieve().bodyToMono(TokenResponse.class).block();
    }

    public record TokenResponse(@JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn, @JsonProperty("scope") String scope,
            @JsonProperty("token_type") String tokenType, @JsonProperty("id_token") String idToken) {
    }

}
