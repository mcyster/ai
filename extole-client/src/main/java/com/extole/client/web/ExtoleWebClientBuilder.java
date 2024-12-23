package com.extole.client.web;

import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

public class ExtoleWebClientBuilder {
    private static final int KEY_LENGTH_MIN = 25;
    private static final int KEY_PEEK_LENGTH = 4;
    
    private static int BUFFER_SIZE = 100 * 1024 * 1024;
    
    WebClient.Builder webClientBuilder;
    Optional<String> clientId = Optional.empty();
    Optional<String> superApiKey = Optional.empty();
    Optional<String> apiKey = Optional.empty();

    private static final Logger logger = LoggerFactory.getLogger(ExtoleWebClientBuilder.class);

    ExtoleWebClientBuilder(String baseUrl) {
        this.webClientBuilder = WebClient.builder()
            .baseUrl(baseUrl)
            //.clientConnector(new ReactorClientHttpConnector(HttpClient.create()
            //        .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
            //        .responseTimeout(Duration.ofSeconds(30))
            //    ))
             .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(BUFFER_SIZE)).build());
    }

    public static ExtoleWebClientBuilder builder(String baseUrl) {
        return new ExtoleWebClientBuilder(baseUrl);
    }

    public ExtoleWebClientBuilder setSuperApiKey(String superApiKey) {
        this.superApiKey = Optional.of(superApiKey);
        return this;
    }

    public ExtoleWebClientBuilder setApiKey(String apiKey) {
        this.apiKey = Optional.of(apiKey);
        return this;
    }

    public ExtoleWebClientBuilder setClientId(String clientId) {
        this.clientId = Optional.of(clientId);
        return this;
    }

    public ExtoleWebClientBuilder enableLogging() {
        this.webClientBuilder.filter(logRequest());
        this.webClientBuilder.filter(logResponse());

        return this;
    }

    public WebClient build() throws ExtoleWebClientException {
        Optional<String> key = Optional.empty();

        if (this.clientId.isEmpty()) {
            if (this.apiKey.isEmpty()) {
                if (this.superApiKey.isPresent()) {
                    key = this.superApiKey;
                    this.clientId = Optional.of("1890234003");
                } else {
                    throw new ExtoleWebClientSuperUserTokenException("No Extole apiKey specified");
                }
            } else {
                key = this.apiKey;
            }
        } else {
            if (this.superApiKey.isPresent()) {
                key = Optional.of(getClientApiKey(this.superApiKey.get(), this.clientId.get()));
                this.apiKey = key;
            } else if (this.apiKey.isPresent()) {
                // TODO verify clientId == access token if unverified
                key = this.apiKey;
            } else {
                throw new ExtoleWebClientTokenException("No Extole apiKey specified");
            }
        }

        if (key.isPresent()) {
            final String bearer = key.get();
            this.webClientBuilder.defaultHeaders(headers -> headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + bearer));
        } else {
            throw new ExtoleWebClientTokenException("No Extole apiKey specified");
        }

        return this.webClientBuilder.build();
    }

    private String getClientApiKey(String superApiKey, String clientId) throws ExtoleWebClientException {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("client_id", clientId);

        JsonNode result;
        try {
            result = this.webClientBuilder.build().post()
                .uri(uriBuilder -> uriBuilder
                    .path("/v4/tokens")
                    .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + superApiKey)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), response ->
                    response.bodyToMono(String.class).flatMap(errorBody ->
                        Mono.error(new ExtoleWebClientException("Invalid extoleManagedApiKey: " + getKeyPeek(superApiKey) + " or clientId: " + clientId 
                              + " bad request code: " + response.statusCode() + " body: " + errorBody + " payload:" + payload.toString()))
                    )
                )
                .bodyToMono(JsonNode.class)
                .block();
        } catch(Throwable exception) {
            if (exception.getCause() instanceof ExtoleWebClientException) {
                throw (ExtoleWebClientException)exception.getCause();
            }
            throw exception;
        }

        if (!result.path("access_token").isEmpty()) {
            throw new ExtoleWebClientTokenException("Internal error, failed to obtain Extole access_token for client: " + clientId);
        }

        return result.path("access_token").asText();
    }

    private static ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            logger.info("Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> logger.info("  " + name
                + ":" + value)));
            return next.exchange(clientRequest);
        };
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.info("Response: " + clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> logger.info("  "
                + name + ":" + value)));
            return Mono.just(clientResponse);
        });
    }

    private static String getKeyPeek(String token) {
        if (token.length() < KEY_LENGTH_MIN) {
            return "Key Bad";
        }

        return "..." + token.substring(token.length() - KEY_PEEK_LENGTH);
    }
}
