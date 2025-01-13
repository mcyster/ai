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

    Asset putAsset(String name, Type mimeType, InputStream content);

    void getAsset(Asset asset, AssetConsumer assetConsumer);

    @FunctionalInterface
    interface AssetConsumer {
        void consume(InputStream content);
    }

    public record Asset(String name, Type type) {
    };

}
