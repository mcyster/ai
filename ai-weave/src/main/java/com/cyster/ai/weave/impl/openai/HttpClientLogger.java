package com.cyster.ai.weave.impl.openai;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import com.cyster.ai.weave.impl.advisor.OperationLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpClientLogger extends HttpClient {

    private final HttpClient delegate;
    private final OperationLogger logger;

    public HttpClientLogger(HttpClient delegate, OperationLogger logger) {
        this.delegate = delegate;
        this.logger = logger.childLogger("Http Client");
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
        logRequest(request);
        HttpResponse<T> response = delegate.send(request, loggingBodyHandler(responseBodyHandler));
        logResponse(response);
        return response;
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        logRequest(request);
        return delegate.sendAsync(request, loggingBodyHandler(responseBodyHandler))
                .thenApply(response -> {
                    logResponse(response);
                    return response;
                });
    }

    private void logRequest(HttpRequest request) {
        logger.log("Request", new Request(request.uri(), request.method(), request.headers().map()));

        request.bodyPublisher().ifPresent(bodyPublisher -> {
            bodyPublisher.subscribe(new LoggingSubscriber(logger));
        });
    }

    private <T> void logResponse(HttpResponse<T> response) {
        logger.log("Response", new Response(response.statusCode(), response.headers().map()));
    }

    private <T> HttpResponse.BodyHandler<T> loggingBodyHandler(HttpResponse.BodyHandler<T> bodyHandler) {
        return responseInfo -> {
            HttpResponse.BodySubscriber<T> bodySubscriber = bodyHandler.apply(responseInfo);
            return new HttpResponse.BodySubscriber<>() {
                @Override
                public CompletionStage<T> getBody() {
                    return bodySubscriber.getBody().thenApply(body -> {
                        if (body instanceof String) {
                            logger.log("Response Body: ", body);
                        }
                        return body;
                    });
                }

                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    bodySubscriber.onSubscribe(subscription);
                }

                @Override
                public void onNext(List<ByteBuffer> item) {
                    bodySubscriber.onNext(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    bodySubscriber.onError(throwable);
                }

                @Override
                public void onComplete() {
                    bodySubscriber.onComplete();
                }
            };
        };
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return delegate.cookieHandler();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return delegate.connectTimeout();
    }

    @Override
    public Redirect followRedirects() {
        return delegate.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return delegate.proxy();
    }

    @Override
    public SSLContext sslContext() {
        return delegate.sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
        return delegate.sslParameters();
    }

    @Override
    public Optional<Executor> executor() {
        return delegate.executor();
    }

    @Override
    public Version version() {
        return delegate.version();
    }


    private static class LoggingSubscriber implements Subscriber<ByteBuffer> {
        private final OperationLogger logger;

        public LoggingSubscriber(OperationLogger logger) {
            this.logger = logger;
        }
        
        @Override
        public void onSubscribe(Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            byte[] bytes = new byte[item.remaining()];
            item.get(bytes);
            logger.log("Request Body", new String(bytes, StandardCharsets.UTF_8));
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
        }
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return delegate.authenticator();
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler,
            PushPromiseHandler<T> pushPromiseHandler) {
        return delegate.sendAsync(request, responseBodyHandler);
    }
    
    public static record Request(URI uri, String method, Map<String,List<String>> headers) {
        @Override
        public String toString() {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static record Response(int code, Map<String,List<String>> headers) {
        @Override
        public String toString() {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
