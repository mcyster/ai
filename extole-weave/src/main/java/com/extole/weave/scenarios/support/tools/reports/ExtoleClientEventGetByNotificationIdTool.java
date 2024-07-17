package com.extole.weave.scenarios.support.tools.reports;

import java.util.Objects;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.extole.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.weave.scenarios.support.tools.ExtoleWebClientFactory;
import com.extole.weave.scenarios.support.tools.reports.ExtoleClientEventGetByNotificationIdTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

//@Component
class ExtoleClientEventGetByNotificationIdTool implements ExtoleSupportTool<Request> {
    Tool<Request, Void> tool;

    ExtoleClientEventGetByNotificationIdTool(ExtoleWebClientFactory extoleWebClientFactory, AiWeaveService aiWeaveService) {
        this.tool = aiWeaveService.cachingTool(new UncachedClientEventGetTool(extoleWebClientFactory));
    }

    @Override
    public String getName() {
        return this.tool.getName();
    }

    @Override
    public String getDescription() {
        return this.tool.getDescription();
    }

    @Override
    public Class<Request> getParameterClass() {
        return this.tool.getParameterClass();
    }

    @Override
    public Object execute(Request parameters, Void context) throws ToolException {
        return this.tool.execute(parameters, context);
    }

    static class Request {
        @JsonProperty(required = true)
        public String clientId;

        @JsonProperty(required = true)
        public String notificationId;

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            Request value = (Request) object;
            return Objects.equals(clientId, value.clientId)
                && Objects.equals(notificationId, value.notificationId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, notificationId);
        }

        @Override
        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException("Error converting object of class " + this.getClass().getName() + " JSON",
                    exception);
            }
        }
    }
}

class UncachedClientEventGetTool implements ExtoleSupportTool<Request> {
    private static final String NOTIFICATION_ID_PATTERN = "[a-z0-9]{18,20}";

    private ExtoleWebClientFactory extoleWebClientFactory;

    UncachedClientEventGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extoleClientEventGetByNotificationId";
    }

    @Override
    public String getDescription() {
        return "Retrieve a client_event by notificationId";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        if (request.notificationId == null || request.notificationId.isBlank()) {
            throw new ToolException("notificationId is required");
        }

        if (!request.notificationId.matches(NOTIFICATION_ID_PATTERN)) {
            throw new ToolException("notificationId " + request.notificationId +
                    " must be 18 to 20 characters and alphanumeric (lowercase alpha only)");
        }

        return getClientEventByNotificationIdViaReport(request);
    }

    private JsonNode getClientEventByNotificationIdViaReport(Request request) throws ToolException {
        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("event_id", request.notificationId);
        }

        var report = new ExtoleReportBuilder(this.extoleWebClientFactory)
            .withClientId(request.clientId)
            .withName("notification_by_event_id")
            .withDisplayName("Notification By Event ID - " + request.notificationId)
            .withParameters(parameters);

        ObjectNode response = report.build();
        if (response == null || response.path("data").isEmpty()) {
            throw new ToolException("Problem searching for notification");
        }

        if (response.path("data").isEmpty() || !response.path("data").isArray()) {
            throw new ToolException("Problem loading notification");
        }

        ArrayNode data = (ArrayNode) response.path("data");
        if (data.size() != 1) {
            throw new ToolException("Notification not found");
        }

        JsonNode notification = data.get(0);
        if (!notification.isObject()) {
            throw new ToolException("Notification invalid");
        }
        ObjectNode notificationNode = (ObjectNode) notification;

        if (!notificationNode.has("client_event")) {
            throw new ToolException("Notification invalid, no client_event");
        }

        return notificationNode.path("client_event");
    }


}
