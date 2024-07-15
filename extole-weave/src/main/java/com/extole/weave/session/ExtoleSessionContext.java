package com.extole.weave.session;

public class ExtoleSessionContext {
    private String accessToken;
    
    public ExtoleSessionContext(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getAccessToken() {
        return this.accessToken;
    }
}
