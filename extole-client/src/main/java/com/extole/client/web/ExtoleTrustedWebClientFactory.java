package com.extole.client.web;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ExtoleTrustedWebClientFactory {
    public final String extoleBaseUri = "https://api.extole.io/";

    private static final Logger logger = LoggerFactory.getLogger(ExtoleTrustedWebClientFactory.class);

    private static final int KEY_LENGTH_MIN = 25;
    private static final int KEY_PEEK_LENGTH = 4;

    private AtomicReference<Optional<String>> extoleSuperUserApiKey = new AtomicReference<Optional<String>>();

    ExtoleTrustedWebClientFactory(
        @Value("${extoleSuperUserApiKey:#{environment.EXTOLE_SUPER_USER_API_KEY}}") String extoleSuperUserApiKey) {
        if (extoleSuperUserApiKey != null) {
            this.extoleSuperUserApiKey.set(Optional.of(extoleSuperUserApiKey));
        } else {
            this.extoleSuperUserApiKey.set(Optional.empty());
            logger.error("extoleSuperUserApiKey not defined or found in environment.EXTOLE_SUPER_USER_API_KEY");
        }
    }

    public WebClient getSuperUserWebClient() throws ExtoleWebClientException {
        if (this.extoleSuperUserApiKey.get().isEmpty()) {
            throw new ExtoleWebClientSuperUserTokenException("extoleSuperUserApiKey is required");
        }

        return ExtoleWebClientBuilder.builder(extoleBaseUri)
            .setSuperApiKey(this.extoleSuperUserApiKey.get().get())
            .build();
    }

    public WebClient getWebClientById(String clientId) throws ExtoleWebClientException {
        if (this.extoleSuperUserApiKey.get().isEmpty()) {
            throw new ExtoleWebClientSuperUserTokenException("extoleSuperUserApiKey is required");
        }

        if (clientId == null || clientId.isBlank()) {
            throw new ExtoleWebClientInvalidIdException("clientId is required");
        }
        if (!clientId.matches("^\\d{1,12}$")) {
            throw new ExtoleWebClientInvalidIdException("A clientId is 1 to 12 digits");
        }

        return ExtoleWebClientBuilder.builder(extoleBaseUri)
            .setSuperApiKey(this.extoleSuperUserApiKey.get().get())
            .setClientId(clientId)
            .enableLogging()
            .build();
    }
    
    // Disable refresh
    // @Scheduled(initialDelay = 5 * 1000, fixedDelay = 60 * 60 * 1000)
    void refreshToken() {
        if (extoleSuperUserApiKey.get().isEmpty()) {
            return;
        }
        Optional<String> token = this.extoleSuperUserApiKey.updateAndGet(key -> refreshSuperApiKey(key));
        logger.info("Refreshed Extole super user key: " + getKeyPeek(token));
    }

    private Optional<String> refreshSuperApiKey(Optional<String> superApiKey) {
        JsonNode response = null;
        try {
            response = getSuperUserWebClient().post()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/tokens")
                    .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + superApiKey.get())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException.Forbidden exception) {
            logger.error("Extole super user key invalid or expired. Key: " + getKeyPeek(superApiKey));
            return superApiKey;
        } catch (ExtoleWebClientException exception) {
            logger.error("Failed to refersh Extole super user key. Key: " + getKeyPeek(superApiKey), exception);
            return superApiKey;
        }

        if (response == null || !response.path("access_token").isEmpty()) {
            logger.error("Failed to refresh Extole super user key: " + getKeyPeek(superApiKey));
            return superApiKey;
        }

        var token = response.path("access_token").asText();

        return Optional.of(token);
    }

    private static String getKeyPeek(Optional<String> token) {
        if (token.isEmpty()) {
            return "No Key";
        }

        if (token.get().length() < KEY_LENGTH_MIN) {
            return "Key Bad";
        }

        return "..." + token.get().substring(token.get().length() - KEY_PEEK_LENGTH);
    }
}
