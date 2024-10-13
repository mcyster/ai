package com.cyster.web.weave.scenarios;


public class WebsiteException extends Exception {
    public WebsiteException(String message) {
        super(message);
    }

    public WebsiteException(String message, Throwable cause) {
        super(message, cause);
    }

}
