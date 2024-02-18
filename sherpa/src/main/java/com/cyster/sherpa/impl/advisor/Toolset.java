package com.cyster.sherpa.impl.advisor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cyster.sherpa.impl.advisor.ToolError.Type;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Toolset<C> {
    private static final Logger logger = LogManager.getLogger(Toolset.class);

    private Map<String, Tool<?, C>> tools = new HashMap<String, Tool<?, C>>();

    private Toolset(List<Tool<?, C>> tools) {
        for (var tool : tools) {
            this.tools.put(tool.getName(), tool);
        }
    }

    public String execute(String name, String jsonParameters, C context) {
        ObjectMapper mapper = new ObjectMapper();

        if (!tools.containsKey(name)) {
            return error("No tool called: " + name, Type.BAD_TOOL_NAME);
        }
        Tool<?, C> tool = tools.get(name);

        try {
            var result = executeTool(tool, jsonParameters, context);

            return mapper.writeValueAsString(result);
        } catch (FatalToolException exception) {
            return error(exception.getMessage(), Type.FATAL_TOOL_ERROR, exception);
        } catch (BadParametersToolException exception) {
            return error(exception.getMessage(), Type.BAD_TOOL_PARAMETERS, exception);
        } catch (ToolException exception) {
            return error(exception.getMessage(), Type.RETRYABLE, exception);
        } catch (JsonProcessingException exception) {
            return error("Tool result could not be formated as json", Type.RETRYABLE, exception);
        } catch (Exception exception) {
            return error("Tool error: " + exception.getMessage(), Type.RETRYABLE, exception);
        }
    }

    public <T> Object executeTool(Tool<T, C> tool, String jsonArguments, C context) throws ToolException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            T parameters = mapper.readValue(jsonArguments, tool.getParameterClass());
            return tool.execute(parameters, context);
        } catch (JsonProcessingException exception) {
            return error("Tool parameters did not match json schema", Type.BAD_TOOL_PARAMETERS, exception);
        }
    }

    public Collection<Tool<?, C>> getTools() {
        return this.tools.values();
    }

    public static class Builder<C> {
        private List<Tool<?, C>> tools = new ArrayList<Tool<?, C>>();

        public <T> Builder<C> addTool(Tool<T, C> tool) {
            this.tools.add(tool);

            return this;
        }

        public Toolset<C> create() {
            return new Toolset<C>(tools);
        }
    }

    private static String error(String message, Type errorType) {
        var response = new ToolError(message, errorType).toJsonString();
        logger.error("ToolError: " + response);

        return response;
    }

    private static String error(String message, Type errorType, Exception exception) {
        var response = new ToolError(message, errorType).toJsonString();
        logger.error("ToolError: " + response, exception);

        return response;
    }
}