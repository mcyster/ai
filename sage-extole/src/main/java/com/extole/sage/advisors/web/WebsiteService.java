package com.extole.sage.advisors.web;

import java.net.URI;
import java.util.List;

public interface WebsiteService {
    List<Website> getSites();
    Website getSite(String name);
    Website create();
    
    interface Website {
        String getId();
        
        URI getUri();
    
        Website putAsset(String name, String content);
    }
    
}
