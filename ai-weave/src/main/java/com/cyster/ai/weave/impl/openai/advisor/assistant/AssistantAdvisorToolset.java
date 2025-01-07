package com.cyster.ai.weave.impl.openai.advisor.assistant;

import java.util.Collections;
import java.util.Map;

import com.cyster.ai.weave.impl.openai.OpenAiSchema;
import com.cyster.ai.weave.impl.openai.advisor.assistant.code.CodeInterpreterToolImpl;
import com.cyster.ai.weave.impl.openai.advisor.assistant.store.SearchToolImpl;
import com.cyster.ai.weave.impl.tool.Toolset;
import com.cyster.ai.weave.service.tool.Tool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;

import io.github.stefanbratanov.jvm.openai.CreateAssistantRequest;
import io.github.stefanbratanov.jvm.openai.Function;

class AssistantAdvisorToolset<CONTEXT> {
    private Toolset<CONTEXT> toolset;

    AssistantAdvisorToolset(Toolset<CONTEXT> toolset) {
        this.toolset = toolset;
    }

    public void applyTools(CreateAssistantRequest.Builder requestBuilder) {
        for (var tool : this.toolset.getTools()) {
            if (tool.getDescription().equals(CodeInterpreterToolImpl.NAME)) {
                // special handling separately
            } else if (tool.getName().equals(SearchToolImpl.NAME)) {
                // special handling separately
            } else {
                var parameterSchema = getOpenAiToolParameterSchema(tool);

                var requestFunction = Function.newBuilder().name(tool.getName()).description(tool.getDescription())
                        .parameters(parameterSchema).build();

                requestBuilder.tool(new io.github.stefanbratanov.jvm.openai.Tool.FunctionTool(requestFunction));
            }
        }
    }

    private static JsonSchema getToolParameterSchema(Tool<?, ?> tool) {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(mapper);

        JsonSchema parameterSchema;
        try {
            parameterSchema = schemaGenerator.generateSchema(tool.getParameterClass());
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }

        return parameterSchema;
    }

    private static Map<String, Object> getOpenAiToolParameterSchema(Tool<?, ?> tool) {
        ObjectMapper mapper = new ObjectMapper();

        if (tool.getParameterClass() == java.lang.Void.class) {
            return Collections.emptyMap();
        }

        var schema = new OpenAiSchema(getToolParameterSchema(tool));

        var schemaNode = schema.toJsonNode();

        return mapper.convertValue(schemaNode, new TypeReference<Map<String, Object>>() {
        });
    }

}
