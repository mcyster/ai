package com.cyster.web.rest.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.cyster.rest.RestException;

import javax.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class WebProxyController {
    private final WebClient webClient;
    private final List<String> allowedBaseUris;
    
    @Autowired
    public WebProxyController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        
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
                .headers(httpHeaders -> headers.forEach(httpHeaders::put))
                .retrieve()
                .toEntity(String.class);
              System.out.println("HERE2");

        } catch(Exception exception) {
            System.out.println("HERE8");
            throw new RestException(HttpStatus.BAD_REQUEST,  "Proxying '" + baseUri + "' failed", exception);
        }

        System.out.println("HERE9");

        return response;
    }

    private URI buildUri(String baseUrl, Map<String, String> parameters) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl);

        parameters.forEach(uriBuilder::queryParam);

        return uriBuilder.build().toUri();
    }
    
}
