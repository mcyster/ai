package com.extole.rest.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.rest.RestException;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@RestController
@RequestMapping("/extole")
public class ExtoleReportRunnerController {
    private final WebClient webClient;
    private final Optional<String> extoleSuperUserApiKey;
    
    @Autowired
    public ExtoleReportRunnerController(
        WebClient.Builder webClientBuilder,        
        @Value("${extoleSuperUserApiKey:#{environment.EXTOLE_SUPER_USER_API_KEY}}") String extoleSuperUserApiKey) {
        
        if (extoleSuperUserApiKey != null) {
            this.extoleSuperUserApiKey = Optional.of(extoleSuperUserApiKey);
        } else {
            this.extoleSuperUserApiKey = Optional.empty();
        }
        
        int bufferSize = 256 * 1024 * 1024; 
        
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(bufferSize))
                        .build())
                .baseUrl("https://api.extole.io")
                .build();        
    }

    @GetMapping("/report-runners/{id}/latest/download.json")
    public Mono<ResponseEntity<String>> getTickets(@PathVariable String id) throws RestException {
        Mono<ResponseEntity<String>> result;
        
        if (this.extoleSuperUserApiKey.isEmpty()) {
            throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, "Extole superuser key not avaliable");
        }

        String url = "/v6/report-runners/" + id + "/latest/download.json";
        try {
            result = this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(url)
                    .build()) 
                .header("Authorization", "Bearer " + this.extoleSuperUserApiKey.get())
                .retrieve()
                .toEntity(String.class);

        } catch(Exception exception) {
            throw new RestException(HttpStatus.BAD_REQUEST,  "Fetching latest report from report runner" + id + " failed, at: " + url, exception);
        }

        return result;
    }
}

