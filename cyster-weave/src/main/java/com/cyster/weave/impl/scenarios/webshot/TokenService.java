package com.cyster.weave.impl.scenarios.webshot;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    private final List<TokenProvider> tokenProviders;

    public TokenService(List<TokenProvider> tokenProviders) {
        if (tokenProviders.isEmpty()) {
            logger.warn("No TokenProviders found. Tokens will not be available.");
        }

        this.tokenProviders = tokenProviders;

    }

    public Optional<String> getToken(String uri) {

        Optional<String> token = Optional.empty();
        for (var provider : this.tokenProviders) {
            if (uri.startsWith(provider.baseUri())) {
                token = Optional.of(provider.getToken());
                logger.info("generated token {} for uri {}", token, uri);
                break;
            }
        }

        return token;
    }
}