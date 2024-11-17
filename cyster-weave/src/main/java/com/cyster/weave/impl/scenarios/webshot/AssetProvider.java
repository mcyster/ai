package com.cyster.weave.impl.scenarios.webshot;

import java.io.InputStream;

public interface AssetProvider {
    enum Type {
        PNG
    }

    AssetId putAsset(Type mimeType, InputStream content);

    void getAsset(AssetId id, AssetConsumer assetConsumer);

    @FunctionalInterface
    interface AssetConsumer {
        void consume(InputStream content);
    }

    public final class AssetId {
        private final String id;

        private AssetId(String id) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("AssetId cannot be null or blank");
            }
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return id;
        }

        public static AssetId fromString(String id) {
            return new AssetId(id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AssetId assetId = (AssetId) o;
            return id.equals(assetId.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
