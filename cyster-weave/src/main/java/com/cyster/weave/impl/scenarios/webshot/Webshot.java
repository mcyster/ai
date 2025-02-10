package com.cyster.weave.impl.scenarios.webshot;

import com.cyster.weave.impl.scenarios.webshot.AssetUrlProvider.AccessibleAsset;

public interface Webshot {

    AccessibleAsset getImage(String name, String url);

}
