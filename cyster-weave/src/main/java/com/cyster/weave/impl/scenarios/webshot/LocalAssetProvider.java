package com.cyster.weave.impl.scenarios.webshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class LocalAssetProvider implements AssetProvider, AssetUrlProvider {
    private static final Logger logger = LoggerFactory.getLogger(LocalAssetProvider.class);

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
    public AssetWriter createAssetWriter(String name, Type type) {
        String baseName = name.replaceAll("[^a-zA-Z0-9-_]", "-").toLowerCase();

        return new LocalAssetWriter(assets, baseName, type);
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
    public AccessibleAsset getAccessibleAsset(Asset asset) {
        return new AccessibleAsset(asset, baseUri.resolve(asset.name()));
    }

    public Path localPath() {
        return assets;
    }

    private static class LocalAssetWriter implements AssetWriter {
        private final Path assets;
        private String baseName;

        private Type type;

        public LocalAssetWriter(Path assets, String baseName, Type type) {
            this.assets = assets;
            this.baseName = baseName;
            this.type = type;
        }

        @Override
        public AssetWriter name(String name) {
            this.baseName = name;
            return this;
        }

        @Override
        public AssetWriter type(Type type) {
            this.type = type;
            return this;
        }

        @Override
        public Asset write(InputStream content) {
            String uniqueName = baseName;
            int counter = 1;
            Path assetPath;
            do {
                String uniquePath = uniqueName + "." + type.toString().toLowerCase();
                assetPath = this.assets.resolve(uniquePath);
                if (!Files.exists(assetPath)) {
                    break;
                }
                uniqueName = baseName + "-" + counter++;
            } while (true);

            try {
                Files.copy(content, assetPath);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write asset to " + assetPath, e);
            }

            return new Asset(uniqueName, type);
        }

    }
}
