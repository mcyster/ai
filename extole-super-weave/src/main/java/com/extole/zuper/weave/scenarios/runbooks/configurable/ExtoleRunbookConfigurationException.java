package com.extole.zuper.weave.scenarios.runbooks.configurable;

import org.springframework.core.io.Resource;

public class ExtoleRunbookConfigurationException extends Exception {
    public ExtoleRunbookConfigurationException(Resource resource, String message) {
        super(buildMessage(resource, message));
    }

    public ExtoleRunbookConfigurationException(Resource resource, String message, Throwable cause) {
        super(buildMessage(resource, message), cause);
    }

    private static String buildMessage(Resource resource, String message) {
        return message + " for resource: " + resource.toString();
    }
}
