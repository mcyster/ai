package com.cyster.web.weave.scenarios;

import java.net.URI;
import java.util.List;

public interface WebsiteProvider {
    List<Website> getSites();
    Website getSite(String name);
    Website create();
    Website copy(Website site) throws WebsiteException;

    interface Website {
        String getId();

        Type getType();

        URI getUri();

        List<String> getAssets();

        Asset putAsset(String name, String content);
        Asset getAsset(String name) throws WebsiteException;

        interface Asset {
            String filename();
            String content();
        }

        enum Type {
            Managed,
            Unmanaged
        }
    }

}
