package com.cyster.weave.impl.scenarios.webshot;

import java.io.InputStream;

public interface AssetProvider {
    enum Type {
        PNG;

        public static Type fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Value cannot be null");
            }
            try {
                return Type.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("No enum constant for value: " + value, e);
            }
        }
    }

    AssetWriter createAssetWriter(String name, Type mimeType);

    void getAsset(Asset asset, AssetConsumer assetConsumer);

    interface AssetWriter {
        AssetWriter name(String name);

        AssetWriter type(Type type);

        Asset write(InputStream content);
    }

    @FunctionalInterface
    interface AssetConsumer {
        void consume(InputStream content);
    }

    public record Asset(String name, Type type) {
    };

}
