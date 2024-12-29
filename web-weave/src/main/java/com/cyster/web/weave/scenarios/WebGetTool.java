package com.cyster.web.weave.scenarios;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.ToolException;
import com.cyster.web.weave.scenarios.WebGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;

import reactor.core.publisher.Mono;

@Component
class WebGetTool implements WebsiteDeveloperTool<Request> {

    private final WebClient webClient;

    WebGetTool(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Fetches the specific url providing the mime type and body";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Class<ManagedWebsites> getContextClass() {
        return ManagedWebsites.class;
    }

    @Override
    public Response execute(Request request, ManagedWebsites context, Weave weave) throws ToolException {
        return webClient.get().uri(request.url).exchangeToMono(response -> handleResponse(response)).block();
    }

    private Mono<Response> handleResponse(ClientResponse response) {
        String mimeType = response.headers().contentType().map(Object::toString).orElse("unknown");

        return response.bodyToMono(byte[].class).map(body -> {
            byte[] uncompressedBody = decompressIfNecessary(body,
                    response.headers().asHttpHeaders().getFirst("Content-Encoding"));
            String bodyAsString = new String(uncompressedBody);
            return new Response(bodyAsString, mimeType);
        });
    }

    private byte[] decompressIfNecessary(byte[] body, String contentEncoding) {
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            return decompressGzip(body);
        } else if ("deflate".equalsIgnoreCase(contentEncoding)) {
            return decompressDeflate(body);
        } else {
            return body;
        }
    }

    private byte[] decompressGzip(byte[] compressedData) {
        try (InputStream stream = new GZIPInputStream(new ByteArrayInputStream(compressedData))) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error decompressing GZIP data", e);
        }
    }

    private byte[] decompressDeflate(byte[] compressedData) {
        try (InputStream stream = new InflaterInputStream(new ByteArrayInputStream(compressedData))) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error decompressing Deflate data", e);
        }
    }

    static record Request(@JsonProperty(required = true) String url) {
    }

    static record Response(String mimeType, String body) {
    }

}
