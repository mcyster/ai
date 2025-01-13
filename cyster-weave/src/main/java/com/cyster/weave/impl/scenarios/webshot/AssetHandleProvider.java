package com.cyster.weave.impl.scenarios.webshot;

import java.net.URI;

import com.cyster.weave.impl.scenarios.webshot.AssetProvider.AssetName;

public interface AssetHandleProvider {

    AssetHandle getAssetHandle(AssetName assetName);

    public static record AssetHandle(AssetName assetName, URI assetUri) {
    };
}
