package com.cyster.ai.weave.impl.advisor.openai;

import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;

public class OpenAiSchema {
    private ObjectMapper mapper = new ObjectMapper();
    private ObjectNode schemaNode;

    public OpenAiSchema(JsonSchema schema) {
        this.schemaNode = mapper.valueToTree(schema);
    }

    public ObjectNode toJsonNode() {
        return transformToOpenAiSchema(this.schemaNode, this.mapper);
    }

    private static ObjectNode transformToOpenAiSchema(ObjectNode schemaNode, ObjectMapper mapper) {

        if (!schemaNode.path("id").isMissingNode()) {
            schemaNode.remove("id");
        }

        ArrayNode requiredNode = mapper.createArrayNode();

        JsonNode propertiesNode = schemaNode.path("properties");
        if (propertiesNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();

                if (fieldValue.isObject()) {
                    ObjectNode fieldObject;
                    if (fieldValue.has("type") && fieldValue.path("type").asText().equals("object")) {
                        fieldObject = transformToOpenAiSchema((ObjectNode) fieldValue, mapper);
                    } else {
                        fieldObject = (ObjectNode) fieldValue;

                        if (!fieldObject.path("required").isMissingNode()) {
                            fieldObject.remove("required");
                            requiredNode.add(fieldName);
                        }
                    }
                }
            }
            schemaNode.set("required", requiredNode);
        }

        return schemaNode;
    }
}
