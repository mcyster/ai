package com.cyster.weave.impl.scenarios.webshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;

public class LocalAssetProvider implements AssetProvider, AssetUrlProvider {
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
    public Asset putAsset(String name, Type type, InputStream content) {

        String extension = "." + type.toString().toLowerCase();
        String baseName = name.replaceAll("[^a-zA-Z0-9-_]", "_").toLowerCase();

        String uniqueName = baseName;
        int counter = 1;
        Path assetPath;
        do {
            String uniquePath = uniqueName + extension;
            assetPath = this.assets.resolve(uniquePath);
            if (!Files.exists(assetPath)) {
                break;
            }
            uniqueName = baseName + "-" + counter++;
        } while (true);

        Asset asset = new Asset(uniqueName, type);

        try {
            Files.copy(content, assetPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write asset to " + assetPath, e);
        }

        return asset;
    }

    @Override
    public void getAsset(Asset asset, AssetConsumer assetConsumer) {
        Path assetPath = this.assets.resolve(asset.name() + "." + asset.type().toString().toLowerCase());

        if (!Files.exists(assetPath)) {
            throw new IllegalArgumentException("Asset " + asset + " does not exist");
        }

        try (InputStream inputStream = Files.newInputStream(assetPath)) {
            assetConsumer.consume(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read asset with ID " + asset, e);
        }
    }

    @Override
    public AccessibleAsset getAccessibleAsset(Asset name) {
        return new AccessibleAsset(name, baseUri.resolve(name.name()));
    }

    public Path localPath() {
        return assets;
    }

    public static Path createUniqueFileName(Path directory, String name, String extension) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        String sanitizedExtension = extension.startsWith(".") ? extension : "." + extension;
        String baseName = name.replaceAll("[^a-zA-Z0-9-_]", "_").toLowerCase();

        Path uniqueFile = directory.resolve(baseName + sanitizedExtension);
        int counter = 1;

        while (Files.exists(uniqueFile)) {
            uniqueFile = directory.resolve(baseName + "-" + counter + sanitizedExtension);
            counter++;
        }

        return uniqueFile;
    }
}
