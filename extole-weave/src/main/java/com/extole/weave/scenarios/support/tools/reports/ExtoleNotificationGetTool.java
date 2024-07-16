package com.extole.weave.scenarios.support.tools.reports;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cyster.ai.weave.service.AiWeaveService;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.extole.weave.scenarios.support.tools.ExtoleSupportTool;
import com.extole.weave.scenarios.support.tools.ExtoleWebClientFactory;
import com.extole.weave.scenarios.support.tools.reports.ExtoleNotificationGetTool.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class ExtoleNotificationGetTool implements ExtoleSupportTool<Request> {
    private Tool<Request, Void> tool;

    ExtoleNotificationGetTool(ExtoleWebClientFactory extoleWebClientFactory, AiWeaveService aiWeaveService) {
        this.tool = aiWeaveService.cachingTool(new UncachedNotificationGetTool(extoleWebClientFactory));
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

        @JsonProperty(required = false)
        public String userId;

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
                && Objects.equals(userId, value.userId)
                && Objects.equals(notificationId, value.notificationId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientId, userId, notificationId);
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

class UncachedNotificationGetTool implements ExtoleSupportTool<Request> {
    private static final String NOTIFICATION_ID_PATTERN = "[a-z0-9]{18,20}";
    private static final String USER_ID_PATTERN = "\\d+";

    private static final Logger logger = LogManager.getLogger(ExtoleSupportTool.class);

    private ExtoleWebClientFactory extoleWebClientFactory;

    UncachedNotificationGetTool(ExtoleWebClientFactory extoleWebClientFactory) {
        this.extoleWebClientFactory = extoleWebClientFactory;
    }

    @Override
    public String getName() {
        return "extoleNotificationGet";
    }

    @Override
    public String getDescription() {
        return "Retrieve a notification by notification_id";
    }

    @Override
    public Class<Request> getParameterClass() {
        return Request.class;
    }

    @Override
    public Object execute(Request request, Void context) throws ToolException {
        JsonNode notification = null;

        if (request.notificationId == null || request.notificationId.isBlank()) {
            throw new ToolException("notificationId is required");
        }

        if (!request.notificationId.matches(NOTIFICATION_ID_PATTERN)) {
            throw new ToolException("notificationId " + request.notificationId +
                    " must be 18 to 20 characters and alphanumeric (lowercase alpha only)");
        }


        if (request.userId != null && !request.userId.isBlank()) {
            if (!request.userId.matches(USER_ID_PATTERN)) {
                throw new ToolException("userId must be 1 or more numeric characters");
            }


            notification = getClientEventByNotificationIdAndUserId(request);
        }
        if (notification == null) {
            notification = getNotificationById(request);
        }

        return notification;
    }

    private JsonNode getClientEventByNotificationIdAndUserId(Request request)
        throws ToolException {
        JsonNode response;
        try {
            response = this.extoleWebClientFactory.getWebClient(request.clientId).get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v6/notifications/" + request.userId)
                    .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        } catch (WebClientResponseException.Forbidden exception) {
            throw new FatalToolException("extoleSuperUserToken is invalid", exception);
        } catch (WebClientException exception) {
            if (exception.getCause() instanceof DataBufferLimitException) {
                logger.warn("Buffer overflow while getting notifications for user: " + request.userId + " at client: " + request.clientId);
                response = null;
            } else {
                throw new ToolException("Internal error, unable to get notications for user: " + request.userId + " at client: " + request.clientId, exception);
            }
        }

        if (response == null || !response.isArray()) {
            return null;
        }
        ArrayNode responseArray = (ArrayNode) response;

        JsonNode notification = null;

        for (int i = 0; i < responseArray.size(); i++) {
            JsonNode notificationNode = responseArray.get(i);

            if (notificationNode.has("event_id")
                && notificationNode.path("event_id").asText().equals(request.notificationId)) {

                notification = notificationNode;
            }
        }

        return notification;
    }

    private JsonNode getNotificationById(Request request) throws ToolException {
        ObjectNode parameters = JsonNodeFactory.instance.objectNode();
        {
            parameters.put("event_id", request.notificationId);
        }

        var reportBuilder = new ExtoleReportBuilder(this.extoleWebClientFactory)
            .withClientId(request.clientId)
            .withName("notification_by_event_id")
            .withDisplayName("Notification By Event ID - " + request.notificationId)
            .withParameters(parameters);

        ObjectNode response = reportBuilder.build();
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

        notificationNode.put("notification_id", notificationNode.get("event_id").asText());
        notificationNode.remove("event_id");

        return notificationNode;

    }


}
