package com.extole.client.web;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import reactor.core.publisher.Mono;
@Component
public class ExtoleWebClientFactory {
    public final String extoleBaseUri = "https://api.extole.io/";

    ExtoleWebClientFactory() {
    }

    public WebClient getWebClient(String accessToken)  {
        return new WebClientBuilder(extoleBaseUri)
            .setApiKey(accessToken)
            .build();

    }

    // TODO merge with ExtoleWebClientBuilder
    public static class WebClientBuilder {
        WebClient.Builder webClientBuilder;

        private static final Logger logger = LogManager.getLogger(ExtoleWebClientBuilder.class);

        public WebClientBuilder(String baseJiraUrl) {
            this.webClientBuilder = WebClient.builder()
                .baseUrl(baseJiraUrl);
        }

        public WebClientBuilder setApiKey(String apiKey) {
            this.webClientBuilder.defaultHeaders(headers ->
                    headers.add("Authorization", "Bearer " + apiKey));
            return this;
        }

        public WebClientBuilder enableLogging() {
            this.webClientBuilder.filter(logRequest());
            this.webClientBuilder.filter(logResponse());

            return this;
        }


        public WebClient build() {
            return this.webClientBuilder.build();
        }

        private static ExchangeFilterFunction logRequest() {
            return (clientRequest, next) -> {
                logger.info("Request: " + clientRequest.method() + " " + clientRequest.url());
                clientRequest.headers().forEach((name, values) -> values.forEach(value -> logger.info("  " + name + ":" + value)));
                return next.exchange(clientRequest);
            };
        }

        private static ExchangeFilterFunction logResponse() {
            return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                logger.info("Response: " + clientResponse.statusCode());
                clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> logger.info("  " + name + ":" + value)));
                return Mono.just(clientResponse);
            });
        }
    }

}

