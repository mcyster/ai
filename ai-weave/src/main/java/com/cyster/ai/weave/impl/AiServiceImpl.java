package com.cyster.ai.weave.impl;

import java.util.Optional;

import com.cyster.ai.weave.impl.openai.advisor.assistant.store.DirectoryDocumentStore;
import com.cyster.ai.weave.impl.openai.advisor.assistant.store.SimpleDocumentStore;
import com.cyster.ai.weave.impl.tool.CachingTool;
import com.cyster.ai.weave.service.AiService;
import com.cyster.ai.weave.service.DocumentStore.DirectoryDocumentStoreBuilder;
import com.cyster.ai.weave.service.DocumentStore.SimpleDocumentStoreBuilder;
import com.cyster.ai.weave.service.tool.Tool;
import com.cyster.ai.weave.service.tool.ToolException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;

public class AiServiceImpl implements AiService {

    public AiServiceImpl() {
    }

    @Override
    public <PARAMETERS, CONTEXT> Tool<PARAMETERS, CONTEXT> cachingTool(Tool<PARAMETERS, CONTEXT> tool) {
        return CachingTool.builder(tool).build();
    }

    @Override
    public SimpleDocumentStoreBuilder simpleDocumentStoreBuilder() {
        return new SimpleDocumentStore.Builder();
    }

    @Override
    public DirectoryDocumentStoreBuilder directoryDocumentStoreBuilder() {
        return new DirectoryDocumentStore.Builder();
    }

    @Override
    public String getJsonSchema(Class<?> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(mapper);

        String schema;
        try {
            JsonSchema jsonSchema = schemaGenerator.generateSchema(clazz);

            JsonNode schemaNode = mapper.valueToTree(jsonSchema);
            if (schemaNode instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) schemaNode;
                objectNode.remove("id");
            }

            schema = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaNode);
        } catch (JsonMappingException e) {
            throw new RuntimeException("Unable to generate schema for " + clazz.getName(), e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to generate schema for " + clazz.getName(), e);
        }
        return schema;
    }

    @Override
    public <RESPONSE> RESPONSE extractResponse(Class<RESPONSE> responseClass, String input) throws ToolException {
        Optional<String> json = extractJson(input);
        if (json.isEmpty()) {
            throw new ToolException("Expected json payload, but input is empty");
        }

        ObjectMapper objectMapper = new ObjectMapper();

        RESPONSE response;
        try {
            var resultNode = objectMapper.readTree(json.get());
            response = objectMapper.treeToValue(resultNode, responseClass);
        } catch (MismatchedInputException exception) {
            throw new ToolException("Json does not contain the required attributes", exception);
        } catch (JsonMappingException exception) {
            throw new ToolException("Json failed to match attributes", exception);
        } catch (JsonProcessingException exception) {
            throw new ToolException("Json passing problems", exception);
        }

        return response;
    }

    private Optional<String> extractJson(String input) {
        int braceCounter = 0;
        int startIndex = -1;
        int endIndex = -1;

        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '{') {
                if (startIndex == -1) {
                    startIndex = i;
                }
                braceCounter++;
            } else if (input.charAt(i) == '}') {
                braceCounter--;
                if (braceCounter == 0 && startIndex != -1) {
                    endIndex = i;
                    break;
                }
            }
        }

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return Optional.of(input.substring(startIndex, endIndex + 1));
        } else {
            return Optional.empty();
        }
    }
}
