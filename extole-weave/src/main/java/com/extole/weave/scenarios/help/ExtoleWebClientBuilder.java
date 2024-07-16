package com.extole.weave.scenarios.help;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class ExtoleWebClientBuilder {
    WebClient.Builder webClientBuilder;

    private static final Logger logger = LogManager.getLogger(ExtoleWebClientBuilder.class);

    ExtoleWebClientBuilder(String baseJiraUrl) {
        this.webClientBuilder = WebClient.builder()
            .baseUrl(baseJiraUrl);
    }

    public static ExtoleWebClientBuilder builder(String baseJiraUrl) {
        return new ExtoleWebClientBuilder(baseJiraUrl);
    }

    public ExtoleWebClientBuilder setApiKey(String apiKey) {
        this.webClientBuilder.defaultHeaders(headers ->
                headers.add("Authorization", "Bearer " + apiKey));
        return this;
    }

    public ExtoleWebClientBuilder enableLogging() {
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
