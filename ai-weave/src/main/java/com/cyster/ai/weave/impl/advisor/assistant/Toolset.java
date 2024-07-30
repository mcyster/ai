package com.cyster.ai.weave.impl.advisor.assistant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyster.ai.weave.impl.advisor.assistant.ToolError.Type;
import com.cyster.ai.weave.service.FatalToolException;
import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class Toolset<C> {
    private static final Logger logger = LoggerFactory.getLogger(Toolset.class);

    private Map<String, Tool<?, C>> tools = new HashMap<String, Tool<?, C>>();

    private Toolset(List<Tool<?, C>> tools) {
        for (var tool : tools) {
            this.tools.put(tool.getName(), tool);
        }
    }

    public String execute(String name, String jsonParameters, C context) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        
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
            return error(exception.getMessage(), exception.getLocalMessage(), Type.RETRYABLE, exception);
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
        return error(message, "", errorType, exception);
    }

    private static String error(String message, String localMessage, Type errorType, Exception exception) {
        var response = new ToolError(message, errorType).toJsonString();

        if (localMessage == null || localMessage.isBlank()) {
            logger.error("ToolError: " + response + localMessage, exception);
        } else {
            logger.error("ToolError: " + response + " localMessage: " + localMessage, exception);
        }

        return response;
    }
}