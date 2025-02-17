package com.cyster.weave.impl.scenarios.webshot;

import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;

public interface WebshotProvider {

    boolean canHandle(String url);

    AccessibleAsset takeSnapshot(String name, String url);
}
