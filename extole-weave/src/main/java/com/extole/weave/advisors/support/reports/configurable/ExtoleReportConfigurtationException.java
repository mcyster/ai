package com.extole.weave.advisors.support.reports.configurable;

import org.springframework.core.io.Resource;

public class ExtoleReportConfigurtationException extends Exception {    
    public ExtoleReportConfigurtationException(String message, Resource resource) {
        super(buildMessage(resource, message));
    }

    public ExtoleReportConfigurtationException(String message, Resource resource, Throwable cause) {
        super(buildMessage(resource, message), cause);
    }
    
    private static String buildMessage(Resource resource, String message) {
        return message + " for resource: " + resource.toString();
    }
}
