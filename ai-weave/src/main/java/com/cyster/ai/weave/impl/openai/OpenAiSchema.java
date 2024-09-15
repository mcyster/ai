package com.cyster.ai.weave.impl.openai;

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
        return transformToOpenAiSchema("", this.schemaNode, this.mapper);
    }

    ObjectNode getJacksonSchema() {
    	return schemaNode;
    }
    
    private static ObjectNode transformToOpenAiSchema(String path, ObjectNode schemaNode, ObjectMapper mapper) {
        
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

                if (fieldValue.has("required")) {
                    if (fieldValue.path("required").asBoolean(false)) {
                        requiredNode.add(fieldName);
                    }
                    ((ObjectNode) fieldValue).remove("required");
                }
            	
                if (fieldValue.isObject()) {
                    if (fieldValue.has("type") && fieldValue.path("type").asText().equals("object")) {
                        transformToOpenAiSchema(path + "." + fieldName, (ObjectNode) fieldValue, mapper);
                    } 
                }
            }

            if (requiredNode.size() > 0) {
                schemaNode.set("required", requiredNode);
            } else {
                schemaNode.remove("required"); 
            }
        }

        if (schemaNode.has("additionalProperties")) {
            JsonNode additionalPropertiesNode = schemaNode.path("additionalProperties");
            if (additionalPropertiesNode.isObject()) {
                ((ObjectNode) additionalPropertiesNode).remove("id");
            }
        }
        
        return schemaNode;
    }
}
