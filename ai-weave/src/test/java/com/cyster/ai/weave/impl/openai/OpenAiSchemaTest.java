package com.cyster.ai.weave.impl.openai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.cyster.ai.weave.impl.openai.OpenAiSchema;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;

public class OpenAiSchemaTest {

    @Test
    public void testOneRequiredAttribute() {
        var schema = new OpenAiSchema(schema(OneRequiredAttribute.class));

        var openAiSchema = schema.toJsonNode();

        System.out.println("openAischema: " + openAiSchema.toPrettyString());
        assertTrue(openAiSchema.has("required"), "The 'required' attribute should exist");
        assertTrue(openAiSchema.path("required").isArray(), "The 'required' attribute is an array");
        assertTrue(openAiSchema.path("required").size() == 1, "The 'required' attribute is of length 1");

        System.out.println("!!required: " + openAiSchema.path("required").toPrettyString());

        System.out.println("!!required.asText: " + openAiSchema.path("required").asText());

        var requiredAttributeFound = false;
        for (JsonNode item : openAiSchema.path("required")) {
            if (item.asText().equals("attribute")) {
                requiredAttributeFound = true;
                break;
            }
        }
        assertTrue(requiredAttributeFound, "The 'required' should contain 'attribute'");

        assertTrue(openAiSchema.has("properties"), "The 'properties' attribute should exist");
        assertTrue(openAiSchema.path("properties").has("attribute"),
            "The path 'properties.attribute' attribute exists");
        assertTrue(openAiSchema.path("properties").path("attribute").has("type"),
            "The path 'properties.attribute.type' attribute exists");
        assertTrue(openAiSchema.path("properties").path("attribute").path("type").asText().equals("string"),
            "The path 'properties.attribute.type' attribute value is String");
        assertTrue(openAiSchema.path("properties").path("attribute").has("description"),
            "The path 'properties.attribute.description' attribute exists");
    }

    @Test
    public void testOneOptionalAttribute() {
        var schema = new OpenAiSchema(schema(OneOptionalAttribute.class));

        var openAiSchema = schema.toJsonNode();

        System.out.println("openAischema: " + openAiSchema.toPrettyString());
        assertFalse(openAiSchema.has("required"), "The 'required' attribute should not exist");

        assertTrue(openAiSchema.has("properties"), "The 'properties' attribute should exist");
        assertTrue(openAiSchema.path("properties").has("attribute"),
            "The path 'properties.attribute' attribute exists");
        assertTrue(openAiSchema.path("properties").path("attribute").has("type"),
            "The path 'properties.attribute.type' attribute exists");
        assertTrue(openAiSchema.path("properties").path("attribute").path("type").asText().equals("string"),
            "The path 'properties.attribute.type' attribute value is String");
        assertTrue(openAiSchema.path("properties").path("attribute").has("description"),
            "The path 'properties.attribute.description' attribute exists");
    }

    @Test
    public void testAttributeWithSubobject() {
        var schema = new OpenAiSchema(schema(AttributeWithSubobject.class));

        var openAiSchema = schema.toJsonNode();

        System.out.println("openAischema: " + openAiSchema.toPrettyString());
        
        assertFalse(openAiSchema.has("required"), "The 'required' attribute should exist");
        
        assertTrue(openAiSchema.has("properties"), "The 'properties' attribute should exist");
        assertTrue(openAiSchema.path("properties").has("attribute"),
            "The path 'properties.attribute' attribute exists");
        assertTrue(openAiSchema.path("properties").path("attribute").has("type"),
            "The path 'properties.attribute.type' attribute exists");
        assertTrue(openAiSchema.path("properties").path("attribute").path("type").asText().equals("string"),
            "The path 'properties.attribute.type' attribute value is String");
        assertTrue(openAiSchema.path("properties").path("attribute").has("description"),
            "The path 'properties.attribute.description' attribute exists");

        assertTrue(openAiSchema.path("properties").has("subobject"),
            "The path 'properties.subobject' exists");
        assertTrue(openAiSchema.path("properties").path("subobject").has("required"),
            "The path 'properties.subobject.required' exists");
        assertTrue(openAiSchema.path("properties").path("subobject").path("required").size() == 1,
            "The path 'properties.subobject.required' has a length of 1");
        assertTrue(openAiSchema.path("properties").path("subobject").path("type").asText().equals("object"),
            "The path 'properties.subobject.type' must be object");
        assertTrue(openAiSchema.path("properties").path("subobject").has("properties"),
            "The path 'properties.subobject.properties' must exist");
    }

    @Test
    public void testNestedMap() {
        var schema = new OpenAiSchema(schema(NestedMap.class));

        var openAiSchema = schema.toJsonNode();

        System.out.println("openAischema: " + openAiSchema.toPrettyString());
        
        assertTrue(openAiSchema.has("required"), "The 'required' attribute should exist");
        assertTrue(openAiSchema.path("required").isArray(), "The 'required' attribute is an array");
        assertTrue(openAiSchema.path("required").size() == 1, "The 'required' attribute is of length 1");
        
        assertTrue(openAiSchema.has("properties"), "The 'property' attribute should exist");
        
        assertTrue(openAiSchema.path("properties").has("attribute"),
            "The path 'properties.attribute' attribute exists");
        assertTrue(openAiSchema.path("properties").path("attribute").has("type"),
            "The path 'properties.attribute.type' attribute exists");
        assertTrue(openAiSchema.path("properties").path("attribute").path("type").asText().equals("string"),
            "The path 'properties.attribute.type' attribute value is String");
        assertTrue(openAiSchema.path("properties").path("attribute").has("description"),
            "The path 'properties.attribute.description' attribute exists");

        assertTrue(openAiSchema.path("properties").has("map"),
                "The path 'properties.map' attribute exists");
        assertTrue(openAiSchema.path("properties").path("map").has("type"),
            "The path 'properties.map.type' attribute exists");
        assertTrue(openAiSchema.path("properties").path("map").path("type").asText().equals("object"),
            "The path 'properties.map.type' attribute value is object");
        assertTrue(openAiSchema.path("properties").path("map").has("description"),
            "The path 'properties.map.description' attribute exists");
        assertTrue(openAiSchema.path("properties").path("map").has("additionalProperties"),
            "The path 'properties.map.additionalProperties' attribute exists");
        assertTrue(openAiSchema.path("properties").path("map").path("additionalProperties").has("type"),
            "The path 'properties.map.additionalProperties.type' attribute exists");
        assertTrue(openAiSchema.path("properties").path("map").path("additionalProperties").path("type").asText().equals("object"),
            "The path 'properties.map.additionalProperties.type' attribute has type object");
        assertFalse(openAiSchema.path("properties").path("map").path("additionalProperties").has("id"),
             "The path 'properties.map.additionalProperties.id' attribute does not exist");

    }
    
    
    private static <T> JsonSchema schema(Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();

        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(mapper);

        JsonSchema schema;
        try {
            schema = schemaGenerator.generateSchema(clazz);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }

        return schema;
    }

}

class OneRequiredAttribute {
    @JsonPropertyDescription("the first and only required attribute")
    @JsonProperty(required = true)
    public String attribute;
}

class OneOptionalAttribute {
    @JsonPropertyDescription("the first and only optional attribute")
    @JsonProperty(required = false)
    public String attribute;
}

class Subobject {
    @JsonPropertyDescription("the required subattribute")
    @JsonProperty(required = true)
    public String subattribute;
}

class AttributeWithSubobject {
    @JsonPropertyDescription("the optional attribute")
    @JsonProperty(required = false)
    public String attribute;

    @JsonPropertyDescription("the optional subobject, which has a required attribute")
    @JsonProperty(required = false)
    public Subobject subobject;
}

record NestedMap (
    @JsonPropertyDescription("Top level attribute") @JsonProperty(required = true) String attribute,
    @JsonPropertyDescription("Map subattribute") @JsonProperty(required = false) Map<String, Object> map) {}
