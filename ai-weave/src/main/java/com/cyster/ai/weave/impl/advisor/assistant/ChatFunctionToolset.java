package com.cyster.ai.weave.impl.advisor.assistant;

import com.cyster.ai.weave.impl.advisor.assistant.ToolError.Type;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.stefanbratanov.jvm.openai.ChatMessage;
import io.github.stefanbratanov.jvm.openai.ChatMessage.ToolMessage;
import io.github.stefanbratanov.jvm.openai.ToolCall.FunctionToolCall;

public class ChatFunctionToolset<C> {
    private Toolset<C> toolset;
    C context = null;

    public ChatFunctionToolset(Toolset<C> toolset) {
        this.toolset = toolset;
    }

    public ChatFunctionToolset<C> withContext(C context) {
        this.context = context;
        return this;
    }

    public ToolMessage call(FunctionToolCall functionToolCall) {
        ObjectMapper objectMapper = new ObjectMapper();

        Object value = toolset.execute(functionToolCall.function().name(), functionToolCall.function().arguments(), this.context);

        JsonNode result = objectMapper.valueToTree(value);

        String json;
        try {
            json = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            return ChatMessage.toolMessage(error("Error converting tool response to json",
                Type.FATAL_TOOL_ERROR, exception), functionToolCall.id());
        }

        return ChatMessage.toolMessage(json, functionToolCall.id());
    }

    private static String error(String message, Type errorType, Exception exception) {
        var response = new ToolError(message, errorType).toJsonString();

        return response;
    }
}
