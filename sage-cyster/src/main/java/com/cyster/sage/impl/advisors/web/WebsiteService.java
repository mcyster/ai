package com.cyster.sage.impl.advisors.web;

import java.net.URI;
import java.util.List;

public interface WebsiteService {
    List<Website> getSites();
    Website getSite(String id);
    Website create();
    
    interface Website {
        String getId();
        
        URI getUri();
    
        List<String> getAssets();
        
        Asset putAsset(String name, String content);
        Asset getAsset(String name);
        
        interface Asset {
            String filename();
            String content();
        }
    }
    

    
}
