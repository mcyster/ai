package com.cyster.web.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.cyster.web.weave.scenarios.WebsiteProvider.Website;

class WebsiteImpl implements Website {
    private final URI uri;
    private final Path directory;
    private final String id;
    private final Type type;

    public WebsiteImpl(URI baseUri, Path baseDirectory, String id, Type type) {
        String path = baseUri.getPath() + "/" + type.name().toLowerCase() + "/" + id + "/index.html";

        this.id = id;
        this.type = type;
        try {
            this.uri = new URI(baseUri.getScheme(), baseUri.getAuthority(), path, baseUri.getQuery(), baseUri.getFragment());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to build website uri");
        }
        this.directory = baseDirectory.resolve(type.name().toLowerCase()).resolve(id);
    }

    public String getId() {
        return this.id;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

    @Override
    public List<String> getAssets() {
        List<String> assets = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.directory)) {
            for (Path entry : stream) {
                assets.add(entry.getFileName().toString());
            }
        } catch (IOException | DirectoryIteratorException exception) {
            throw new RuntimeException("Unable to find website directory: " + this.directory, exception);
        }

        return assets;
    };

    @Override
    public Asset putAsset(String name, String content) {
        try {
            if (!Files.exists(this.directory)) {
                Files.createDirectories(this.directory);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Unable to create page directory", exception);
        }

        Path file = this.directory.resolve(name);
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException exception) {
                throw new RuntimeException("Unable to create file: " + name, exception);
            }
        }

        try {
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new RuntimeException("Unable to write content to file", exception);
        }

        return new TextAsset(name, content);
    }

    @Override
    public Asset getAsset(String name) {
        if (!Files.exists(directory)) {
            throw new RuntimeException("Unable to find website directory: " + directory);
        }

        Path file = directory.resolve(name);
        if (!Files.exists(file)) {
            throw new RuntimeException("Unable to find file: " + name);
        }

        String content;
        try {
            content = Files.readString(file);
        } catch (IOException exception) {
            throw new RuntimeException("Unable to read file: " + name);
        }

        return new TextAsset(name, content);
    }

    public static record TextAsset(String filename, String content) implements Asset {
    }

}
