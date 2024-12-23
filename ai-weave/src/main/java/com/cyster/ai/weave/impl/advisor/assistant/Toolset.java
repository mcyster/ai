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
import com.cyster.ai.weave.service.ToolContextException;
import com.cyster.ai.weave.service.ToolContextFactory;
import com.cyster.ai.weave.service.ToolException;
import com.cyster.ai.weave.service.Weave;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Toolset {
    private static final Logger logger = LoggerFactory.getLogger(Toolset.class);

    private final ToolContextFactory toolContextFactory;
    private final Map<String, Tool<?, ?>> tools = new HashMap<String, Tool<?, ?>>();

    private Toolset(ToolContextFactory toolContextFactory, List<Tool<?, ?>> tools) {
        this.toolContextFactory = toolContextFactory;
        for (var tool : tools) {
            this.tools.put(tool.getName(), tool);
        }
    }

    public <SCENARIO_CONTEXT> String execute(String name, String jsonParameters, SCENARIO_CONTEXT scenarioContext,
            Weave weave) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModules(new JavaTimeModule());

        if (!tools.containsKey(name)) {
            return error("No tool called: " + name, Type.BAD_TOOL_NAME);
        }
        Tool<?, ?> tool = tools.get(name);

        try {
            var result = executeTool(tool, jsonParameters, scenarioContext, weave);

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

    public <TOOL_PARAMETERS, TOOL_CONTEXT, SCENARIO_CONTEXT> Object executeTool(
            Tool<TOOL_PARAMETERS, TOOL_CONTEXT> tool, String jsonArguments, SCENARIO_CONTEXT scenarioContext,
            Weave weave) throws ToolException {
        ObjectMapper mapper = new ObjectMapper();

        TOOL_CONTEXT toolContext = toolContextFactory.createContext(tool.getContextClass(), scenarioContext);

        try {
            TOOL_PARAMETERS parameters = mapper.readValue(jsonArguments, tool.getParameterClass());
            return tool.execute(parameters, toolContext, weave);
        } catch (MismatchedInputException exception) {
            // Original Message can help describe the problem in enough detail to resolve,
            // often references the exact field.
            return error("Tool parameters did not match json schema. " + exception.getOriginalMessage(),
                    Type.BAD_TOOL_PARAMETERS, exception);
        } catch (JsonProcessingException exception) {
            return error("Tool parameters did not match json schema", Type.BAD_TOOL_PARAMETERS, exception);
        }
    }

    public Collection<Tool<?, ?>> getTools() {
        return this.tools.values();
    }

    public static class Builder {
        private final ToolContextFactory toolContextFactory;
        private final List<Tool<?, ?>> tools = new ArrayList<Tool<?, ?>>();

        public Builder(ToolContextFactory toolContextFactory) {
            this.toolContextFactory = toolContextFactory;
        }

        public <TOOL_PARAMETERS, TOOL_CONTEXT> Builder addTool(Tool<TOOL_PARAMETERS, TOOL_CONTEXT> tool) {
            this.tools.add(tool);

            return this;
        }

        public Toolset create() {
            return new Toolset(toolContextFactory, tools);
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