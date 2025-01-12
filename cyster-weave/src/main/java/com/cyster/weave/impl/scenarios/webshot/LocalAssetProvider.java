package com.cyster.weave.impl.scenarios.webshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

public class LocalAssetProvider implements AssetProvider {
    private URI baseUri;
    private final Path assets;

    public LocalAssetProvider(URI baseUri, @Value("${AI_HOME}") String aiHome) {
        this.baseUri = baseUri;

        Path directory = Paths.get(aiHome);
        if (!Files.exists(directory)) {
            throw new IllegalArgumentException("AI_HOME (" + aiHome + ") does not exist");
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("AI_HOME (" + aiHome + ") is not a directory");
        }

        this.assets = directory.resolve("assets");
        try {
            if (!Files.exists(this.assets)) {
                Files.createDirectories(this.assets);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create assets directory: " + this.assets, e);
        }
    }

    @Override
    public AssetId putAsset(Type type, InputStream content) {
        AssetId assetId = AssetId.fromString(UUID.randomUUID().toString() + "." + type.toString().toLowerCase());
        Path assetPath = this.assets.resolve(assetId.toString());

        try {
            Files.copy(content, assetPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write asset to " + assetPath, e);
        }

        return assetId;
    }

    @Override
    public void getAsset(AssetId id, AssetConsumer assetConsumer) {
        Path assetPath = this.assets.resolve(id.toString());

        if (!Files.exists(assetPath)) {
            throw new IllegalArgumentException("Asset with ID " + id + " does not exist");
        }

        try (InputStream inputStream = Files.newInputStream(assetPath)) {
            assetConsumer.consume(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read asset with ID " + id, e);
        }
    }

    public URI getAssetUri(AssetId id) {
        return baseUri.resolve(id.getId());
    }

    public Path localPath() {
        return assets;
    }
}
