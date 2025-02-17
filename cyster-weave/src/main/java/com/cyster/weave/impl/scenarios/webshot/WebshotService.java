package com.cyster.weave.impl.scenarios.webshot;

import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;

public interface WebshotService {

    AccessibleAsset takeSnapshot(String name, String url);
}
