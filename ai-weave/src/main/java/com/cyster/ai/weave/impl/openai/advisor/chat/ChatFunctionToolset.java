package com.cyster.ai.weave.impl.openai.advisor.chat;

import com.cyster.ai.weave.impl.tool.ToolError;
import com.cyster.ai.weave.impl.tool.Toolset;
import com.cyster.ai.weave.impl.tool.ToolError.Type;
import com.cyster.ai.weave.service.Weave;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.stefanbratanov.jvm.openai.ChatMessage;
import io.github.stefanbratanov.jvm.openai.ChatMessage.ToolMessage;
import io.github.stefanbratanov.jvm.openai.ToolCall.FunctionToolCall;

public class ChatFunctionToolset<CONTEXT> {
    private Toolset toolset;
    CONTEXT context = null;
    Weave weave = null;

    public ChatFunctionToolset(Toolset toolset) {
        this.toolset = toolset;
    }

    public ChatFunctionToolset<CONTEXT> withContext(CONTEXT context) {
        this.context = context;
        return this;
    }

    public ChatFunctionToolset<CONTEXT> withWeave(Weave weave) {
        this.weave = weave;
        return this;
    }

    public ToolMessage call(FunctionToolCall functionToolCall) {
        ObjectMapper objectMapper = new ObjectMapper();

        Object value = toolset.execute(functionToolCall.function().name(), functionToolCall.function().arguments(),
                this.context, weave);

        JsonNode result = objectMapper.valueToTree(value);

        String json;
        try {
            json = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            return ChatMessage.toolMessage(
                    error("Error converting tool response to json", Type.FATAL_TOOL_ERROR, exception),
                    functionToolCall.id());
        }

        return ChatMessage.toolMessage(json, functionToolCall.id());
    }

    private static String error(String message, Type errorType, Exception exception) {
        var response = new ToolError(message, errorType).toJsonString();

        return response;
    }
}
