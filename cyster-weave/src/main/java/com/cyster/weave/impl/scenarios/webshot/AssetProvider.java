package com.cyster.weave.impl.scenarios.webshot;

import java.io.InputStream;

public interface AssetProvider {
    enum Type {
        PNG
    }

    AssetName putAsset(String name, Type mimeType, InputStream content);

    void getAsset(AssetName name, AssetConsumer assetConsumer);

    @FunctionalInterface
    interface AssetConsumer {
        void consume(InputStream content);
    }

    public final class AssetName {
        private final String name;

        private AssetName(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("AssetName cannot be null or blank");
            }
            this.name = name;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static AssetName fromString(String name) {
            return new AssetName(name);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            AssetName assetId = (AssetName) object;
            return name.equals(assetId.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
