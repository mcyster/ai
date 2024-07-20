package com.cyster.web.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.cyster.rest.RestException;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class WebProxyController {
    private final WebClient webClient;
    private final List<String> allowedBaseUris;
    
    @Autowired
    public WebProxyController(WebClient.Builder webClientBuilder) {
        int bufferSize = 256 * 1024 * 1024; 
        
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(bufferSize))
                        .build())
                .build();
        
        this.allowedBaseUris = new ArrayList<>();
        this.allowedBaseUris.add("https://api.extole.io");
    }

    @GetMapping("/proxy/{*path}")
    public Mono<ResponseEntity<String>> proxyGetRequest(
        @PathVariable("path") String path,
        @RequestParam Map<String, String> parameters,
        @RequestHeader HttpHeaders headers) throws RestException {

        System.out.println("HERE1: " + path);
        String baseUri = (path.startsWith("/")) ? path.substring(1) : path;

        
        var allowed = false;
        for(var allowedBaseUri: this.allowedBaseUris) {
            if (baseUri.startsWith(allowedBaseUri)) {
                allowed = true;
            }
        }
        if (!allowed) {
            throw new RestException(HttpStatus.BAD_REQUEST,  "Proxying '" + baseUri + "' is not allowed");
        }
        
        Mono<ResponseEntity<String>> response;
        try {
            response = webClient.get()
                .uri(uriBuilder -> {
                    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUri);
                    parameters.forEach(builder::queryParam);
                    return builder.build().toUri();
                })
                .headers(httpHeaders -> {
                    headers.forEach(httpHeaders::put);
                    httpHeaders.remove(HttpHeaders.ACCEPT_ENCODING); // Remove the Accept-Encoding header
                }) 
                .retrieve()
                .toEntity(String.class);
              System.out.println("HERE2");

        } catch(Exception exception) {
            System.out.println("HERE8");
            throw new RestException(HttpStatus.BAD_REQUEST,  "Proxying '" + baseUri + "' failed", exception);
        }

        System.out.println(response.block().getBody());

        return response;
    }
    
}
