package com.cyster.ai.weave.impl.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cyster.ai.weave.impl.tool.ToolError.Type;
import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.tool.FatalToolException;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.ToolContextException;
import com.cyster.ai.weave.service.tool.ToolException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Toolset<CONTEXT> {
    private static final Logger logger = LoggerFactory.getLogger(Toolset.class);

    private final Map<String, Tool<?, CONTEXT>> tools = new HashMap<>();

    private Toolset(List<Tool<?, CONTEXT>> tools) {
        for (var tool : tools) {
            this.tools.put(tool.getName(), tool);
        }
    }

    public String execute(String name, String jsonParameters, CONTEXT context, Weave weave) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModules(new JavaTimeModule());

        if (!tools.containsKey(name)) {
            return error("No tool called: " + name, Type.BAD_TOOL_NAME);
        }
        Tool<?, CONTEXT> tool = tools.get(name);

        try {
            var result = executeTool(tool, jsonParameters, context, weave);

            return mapper.writeValueAsString(result);
        } catch (FatalToolException exception) {
            return error(exception.getMessage(), Type.FATAL_TOOL_ERROR, exception);
        } catch (BadParametersToolException exception) {
            return error(exception.getMessage(), Type.BAD_TOOL_PARAMETERS, exception);
        } catch (ToolContextException exception) {
            return error(exception.getMessage(), "ToolContextFactory dosn't support context", Type.FATAL_TOOL_ERROR,
                    exception);
        } catch (ToolException exception) {
            return error(exception.getMessage(), exception.getLocalMessage(), Type.RETRYABLE, exception);
        } catch (JsonProcessingException exception) {
            return error("Tool result could not be formated as json", Type.RETRYABLE, exception);
        } catch (Exception exception) {
            return error("Tool error: " + exception.getMessage(), Type.RETRYABLE, exception);
        }
    }

    public <PARAMETERS> Object executeTool(Tool<PARAMETERS, CONTEXT> tool, String jsonArguments, CONTEXT context,
            Weave weave) throws ToolException {
        ObjectMapper mapper = new ObjectMapper();

        try {
            var parameters = mapper.readValue(jsonArguments, tool.getParameterClass());
            return tool.execute(parameters, context, weave);
        } catch (MismatchedInputException exception) {
            // Original Message can help describe the problem in enough detail to resolve,
            // often references the exact field.
            return error("Tool parameters did not match json schema. " + exception.getOriginalMessage(),
                    Type.BAD_TOOL_PARAMETERS, exception);
        } catch (JsonProcessingException exception) {
            return error("Tool parameters did not match json schema", Type.BAD_TOOL_PARAMETERS, exception);
        }
    }

    public Collection<Tool<?, CONTEXT>> getTools() {
        return this.tools.values();
    }

    public static class Builder<CONTEXT> {
        private final List<Tool<?, CONTEXT>> tools = new ArrayList<>();

        public Builder() {
        }

        public <PARAMETERS> Builder<CONTEXT> addTool(Tool<PARAMETERS, CONTEXT> tool) {
            this.tools.add(tool);

            return this;
        }

        public Toolset<CONTEXT> create() {
            return new Toolset<CONTEXT>(tools);
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