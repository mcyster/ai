package com.cyster.jira.client.web;

import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class JiraWebClientBuilder {
    private static int BUFFER_SIZE = 160 * 1024 * 1024;

    WebClient.Builder webClientBuilder;

    private static final Logger logger = LoggerFactory.getLogger(JiraWebClientBuilder.class);

    JiraWebClientBuilder(String baseJiraUrl) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(BUFFER_SIZE))
                .build();
        
        this.webClientBuilder = WebClient.builder()
            .exchangeStrategies(strategies)
            .baseUrl(baseJiraUrl);
    }

    public static JiraWebClientBuilder builder(String baseJiraUrl) {
        return new JiraWebClientBuilder(baseJiraUrl);
    }

    public JiraWebClientBuilder setApiKey(String apiKey) {
        if (apiKey.contains(":")) {
            String[] keyParts = apiKey.split(":");
            if (keyParts.length != 2) {
                throw new RuntimeException("Jira Key Bad");
            }
            this.webClientBuilder.defaultHeaders(headers ->
                headers.setBasicAuth(keyParts[0], keyParts[1], StandardCharsets.UTF_8 ));
        } else {
            this.webClientBuilder.defaultHeaders(headers ->
                headers.add("Authorization", "Bearer " + apiKey));
        }

        return this;
    }

    public JiraWebClientBuilder enableLogging() {
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
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> logger.info(name + ":" + value)));
            return next.exchange(clientRequest);
        };
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.info("Response: " + clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> logger.info(name + ":" + value)));
            return Mono.just(clientResponse);
        });
    }
}
