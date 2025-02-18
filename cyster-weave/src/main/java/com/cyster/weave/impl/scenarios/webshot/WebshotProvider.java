package com.cyster.weave.impl.scenarios.webshot;

import com.cyster.weave.impl.scenarios.webshot.AssetProvider.Asset;
import com.cyster.weave.impl.scenarios.webshot.AssetProvider.AssetWriter;

public interface WebshotProvider {

    boolean canHandle(String url);

    Asset takeSnapshot(AssetWriter writer, String url);
}
