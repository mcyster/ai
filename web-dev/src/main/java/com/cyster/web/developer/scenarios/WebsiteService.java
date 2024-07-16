package com.cyster.web.developer.scenarios;

import java.net.URI;
import java.util.List;

public interface WebsiteService {
    List<Website> getSites();
    Website getSite(String name);
    Website create();
    Website name(Website website, String name);
    Website copy(Website site);

    interface Website {
        String getId();

        Type getType();

        URI getUri();

        List<String> getAssets();

        Asset putAsset(String name, String content);
        Asset getAsset(String name);

        interface Asset {
            String filename();
            String content();
        }

        enum Type {
            Temporary,
            Named,
            Managed
        }
    }

}
