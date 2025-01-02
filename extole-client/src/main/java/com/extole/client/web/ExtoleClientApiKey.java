package com.extole.client.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

public class ExtoleClientApiKey {
    public final String extoleBaseUri = "https://api.extole.io/";

    private static final int KEY_LENGTH_MIN = 25;
    private static final int KEY_PEEK_LENGTH = 4;

    private final WebClient.Builder webClientBuilder;

    public ExtoleClientApiKey() {
        this.webClientBuilder = WebClient.builder().baseUrl(extoleBaseUri);
    }

    public String getClientApiKey(String superApiKey, String clientId) throws ExtoleWebClientException {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("client_id", clientId);

        JsonNode result;
        try {
            result = this.webClientBuilder.build().post().uri(uriBuilder -> uriBuilder.path("/v4/tokens").build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + superApiKey).accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON).bodyValue(payload).retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new ExtoleWebClientException(
                                            "Invalid extoleManagedApiKey: " + getKeyPeek(superApiKey) + " or clientId: "
                                                    + clientId + " bad request code: " + response.statusCode()
                                                    + " body: " + errorBody + " payload:" + payload.toString()))))
                    .bodyToMono(JsonNode.class).block();
        } catch (Throwable exception) {
            if (exception.getCause() instanceof ExtoleWebClientException) {
                throw (ExtoleWebClientException) exception.getCause();
            }
            throw exception;
        }

        if (!result.path("access_token").isEmpty()) {
            throw new ExtoleWebClientTokenException(
                    "Internal error, failed to obtain Extole access_token for client: " + clientId);
        }

        return result.path("access_token").asText();
    }

    private static String getKeyPeek(String token) {
        if (token.length() < KEY_LENGTH_MIN) {
            return "Key Bad";
        }

        return "..." + token.substring(token.length() - KEY_PEEK_LENGTH);
    }
}
