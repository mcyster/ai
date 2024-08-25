package com.extole.rest.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyster.rest.RestException;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.client.web.ExtoleWebClientException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/extole")
public class ExtoleReportController {
    private final Optional<String> extoleSuperUserApiKey;
    private final ExtoleTrustedWebClientFactory extoleTrustedWebClientFactory;
    
    @Autowired
    public ExtoleReportController(
        ExtoleTrustedWebClientFactory extoleTrustedWebClientFactory,        
        @Value("${extoleSuperUserApiKey:#{environment.EXTOLE_SUPER_USER_API_KEY}}") String extoleSuperUserApiKey) {
        
        if (extoleSuperUserApiKey != null) {
            this.extoleSuperUserApiKey = Optional.of(extoleSuperUserApiKey);
        } else {
            this.extoleSuperUserApiKey = Optional.empty();
        }
                
        this.extoleTrustedWebClientFactory = extoleTrustedWebClientFactory;   
    }

    @GetMapping("/{clientId}/reports/{reportId}/download.json")
    public Mono<ResponseEntity<Flux<String>>> getTickets(@PathVariable String clientId, @PathVariable String reportId) throws RestException {
        if (this.extoleSuperUserApiKey.isEmpty()) {
            throw new RestException(HttpStatus.INTERNAL_SERVER_ERROR, "Extole superuser key not available");
        }

        String url = "/v4/reports/" + reportId + "/download.json";

        try {
            return this.extoleTrustedWebClientFactory.getWebClientById(clientId).get()
                .uri(uriBuilder -> uriBuilder
                    .path(url)
                    .build())
                .retrieve()
                .bodyToFlux(String.class)
                .onErrorResume(exception -> {
                    return Mono.error(
                        new RestException(HttpStatus.BAD_REQUEST, "Fetching report " + reportId + " for clientId " + clientId + " failed, at: " + url, exception)
                    );
                })
                .collectList()
                .flatMap(list -> {
                    return Mono.just(
                        ResponseEntity.ok()
                            .contentType(MediaType.TEXT_EVENT_STREAM) 
                            .body(Flux.fromIterable(list))
                    );
                });
        } catch (ExtoleWebClientException exception) {
            throw new RuntimeException("Fetching report " + reportId + " for clientId " + clientId + " failed, at: " + url, exception);
        }         
    }
}
