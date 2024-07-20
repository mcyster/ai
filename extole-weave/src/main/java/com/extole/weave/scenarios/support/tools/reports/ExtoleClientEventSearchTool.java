package com.extole.weave.scenarios.support.tools.reports;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.ToolException;
import com.extole.client.web.ExtoleTrustedWebClientFactory;
import com.extole.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.weave.scenarios.support.tools.reports.ExtoleClientEventSearchTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
class ExtoleClientEventSearchTool implements ExtoleSupportTool<Request> {
    private static final String NOTIFICATION_ID_PATTERN = "[a-z0-9]{18,20}";

    private ExtoleTrustedWebClientFactory extoleWebClientFactory;
    private ExtoleNotificationGetTool extoleNotificationGetTool;

    ExtoleClientEventSearchTool(ExtoleTrustedWebClientFactory extoleWebClientFactory, ExtoleNotificationGetTool extoleNotificationGetTool) {
        this.extoleWebClientFactory = extoleWebClientFactory;
        this.extoleNotificationGetTool = extoleNotificationGetTool;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName().replace("Tool", "");
    }

    @Override
    public String getDescription() {
        return "Finds client events by event name or user";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        var tags = "";
        if (request.tags != null) {
            tags = request.tags;
        }

        if (request.likeNotificationId != null) {
            if (!request.likeNotificationId.matches(NOTIFICATION_ID_PATTERN)) {
                throw new ToolException("likeNotificationId " + request.likeNotificationId +
                        " must be 18 to 20 characters and alphanumeric (lowercase alpha only)");
            }

            var notificationRequest = new com.extole.weave.scenarios.support.tools.reports.ExtoleNotificationGetTool.Request();
            notificationRequest.clientId = request.clientId;
            notificationRequest.userId = request.userId;
            notificationRequest.notificationId = request.likeNotificationId;

            JsonNode notification = (JsonNode)this.extoleNotificationGetTool.execute(notificationRequest, null);

            JsonNode tagsNode = new ObjectMapper().createArrayNode();
            if (notification.has("tags")) {
                tagsNode = notification.path("tags");
            }
            else if (notification.has("client_event") && notification.path("client_event").has("tags")) {
                tagsNode = notification.path("client_event").path("tags");
            }

            if (tagsNode.isArray()) {
                for (JsonNode tagNode : tagsNode) {
                    if (tags.length() > 0 && !tags.toString().trim().isEmpty()) {
                        tags = tags + ",";
                    }
                    tags = tags + tagNode.asText();
                }
            }
        }

        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("time_range", "LAST_QUARTER");
            if (request.userId != null) {
                parameters.put("event_user", request.userId);
            }
            if (request.eventName != null) {
                parameters.put("event_name", request.eventName);
            }
            if (!tags.isBlank()) {
                parameters.put("matching_all_tags", tags);
            }
        }

        var reportBuilder = new ExtoleReportBuilder(this.extoleWebClientFactory)
            .withClientId(request.clientId)
            .withLimit(2)
            .withName("client_events")
            .withDisplayName("Client Events - tags:" +  tags)
            .withParameters(parameters)
            .withWaitForResult(false);

        return reportBuilder.build();
    }

    static class Request {
        @JsonProperty(required = true)
        public String clientId;

        @JsonPropertyDescription("Query for client events like the client event that triggered the notification with this id")
        @JsonProperty(required = false)
        public String likeNotificationId;


        @JsonPropertyDescription("Query for client events caused by user_id")
        @JsonProperty(required = false)
        public String userId;


        @JsonPropertyDescription("Query client events by tags, a comma seperated list of tags.")
        @JsonProperty(required = false)
        public String tags;

        @JsonPropertyDescription("Query client events by event_name")
        @JsonProperty(required = false)
        public String eventName;

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
                && Objects.equals(userId, value.userId)
                && Objects.equals(eventName, value.eventName)
                && Objects.equals(tags, value.tags);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, tags);
//            return Objects.hash(clientId, eventName, tags);

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


