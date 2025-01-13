package com.cyster.weave.impl.scenarios.webshot;

import java.net.URI;

import com.cyster.weave.impl.scenarios.webshot.AssetProvider.Asset;

public interface AssetUrlProvider {

    AccessibleAsset getAccessibleAsset(Asset assetName);

    public static record AccessibleAsset(Asset assetName, URI assetUri) {
    };
}
