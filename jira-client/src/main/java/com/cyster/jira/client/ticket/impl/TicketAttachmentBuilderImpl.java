package com.cyster.jira.client.ticket.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import com.cyster.jira.client.ticket.TicketAttachmentBuilder;
import com.cyster.jira.client.ticket.TicketException;
import com.cyster.jira.client.web.JiraWebClientFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

public class TicketAttachmentBuilderImpl implements TicketAttachmentBuilder {
    private final JiraWebClientFactory jiraWebClientFactory;
    private final String key;
    private File assetFile;

    TicketAttachmentBuilderImpl(JiraWebClientFactory jiraWebClientFactory, String key) {
        this.jiraWebClientFactory = jiraWebClientFactory;
        this.key = key;
    }

    @Override
    public TicketAttachmentBuilder withAsset(String name, String type, InputStream inputStream) {
        String extension = type.startsWith(".") ? type : "." + type;
        try {
            this.assetFile = File.createTempFile(name + "-", extension);
            assetFile.deleteOnExit();

            try (var outputStream = new FileOutputStream(assetFile)) {
                inputStream.transferTo(outputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary file for input stream", e);
        }
        return this;
    }

    @Override
    public Attachment post() throws TicketException {
        FileSystemResource resource = new FileSystemResource(assetFile);

        try {
            String attachmentResponse = this.jiraWebClientFactory.getWebClient().post()
                    .uri(uriBuilder -> uriBuilder.path("/rest/api/3/issue/" + key + "/attachments").build())
                    .header("X-Atlassian-Token", "no-check").contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData("file", resource)).retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .map(errorBody -> new TicketException("Error uploading attachment to ticket " + key
                                            + ": " + response.statusCode() + " Body: " + errorBody))
                                    .flatMap(Mono::error))
                    .bodyToMono(String.class).block();

            return extractAttachmentUrlFromResponse(attachmentResponse);
        } finally {
            if (assetFile.exists()) {
                assetFile.delete();
            }
        }

    }

    private Attachment extractAttachmentUrlFromResponse(String responseJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode responseNode = objectMapper.readTree(responseJson);
            var id = responseNode.get(0).path("id").asText();
            var filename = responseNode.get(0).path("filename").asText();
            var uri = new URI(responseNode.get(0).path("content").asText());
            return new Attachment(id, filename, uri);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException("Failed to parse attachment, response: " + exception.getMessage(), exception);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to get url for asset, response: " + e.getMessage(), e);
        }
    }

}
