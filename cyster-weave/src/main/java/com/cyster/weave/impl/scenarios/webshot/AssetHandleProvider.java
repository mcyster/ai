package com.cyster.weave.impl.scenarios.webshot;

import java.net.URI;

import com.cyster.weave.impl.scenarios.webshot.AssetProvider.AssetId;

public interface AssetHandleProvider {

    AssetHandle getAssetHandle(AssetId assetId);

    public static record AssetHandle(AssetId assetId, URI assetUri) {
    };
}
