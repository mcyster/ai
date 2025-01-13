package com.cyster.weave.impl.scenarios.webshot;

import java.io.InputStream;

import org.springframework.core.io.FileSystemResource;

public interface AssetProvider {
    enum Type {
        PNG
    }

    AssetId putAsset(Type mimeType, InputStream content);

    void getAsset(AssetId id, AssetConsumer assetConsumer);

    // TBD re-read stream problems on getAsset(id,consumer)
    FileSystemResource getAsset(AssetId id);

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

        public String id() {
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
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            AssetId assetId = (AssetId) object;
            return id.equals(assetId.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
