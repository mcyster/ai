package com.cyster.ai.weave.impl.openai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;

public class OpenAiSchemaTest {

    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        System.out.println("Running test: " + testInfo.getDisplayName());
    }
	  
    @Test
    public void testOneRequiredAttribute() {
        String expectedSchema = """
{
  "type" : "object",
  "properties" : {
    "attribute" : {
      "type" : "string",
      "description" : "the first and only required attribute"
    }
  },
  "required" : [ "attribute" ]
}        		
""";
        assertTrue(check(OneRequiredAttribute.class, expectedSchema), "generated schema does not match expected schema");
    }

    @Test
    public void testOneOptionalAttribute() {
        String expectedSchema = """
{
  "type" : "object",
  "properties" : {
    "attribute" : {
      "type" : "string",
      "description" : "the first and only optional attribute"
    }
  }
}	
""";
        assertTrue(check(OneOptionalAttribute.class, expectedSchema), "generated schema does not match expected schema");
    }

    @Test
    public void testAttributeWithSubobject() {
        String expectedSchema = """
{
  "type": "object",
  "properties": {
    "attribute": {
      "type": "string",
      "description": "the optional attribute"
    },
    "subobject": {
      "type": "object",
      "description": "the optional subobject, which has a required attribute",
      "properties": {
        "subattribute": {
          "type": "string",
          "description": "the required subattribute"
        }
      },
      "required": ["subattribute"]
    }
  }
}
""";
        assertTrue(check(AttributeWithSubobject.class, expectedSchema), "generated schema does not match expected schema");
    }

    @Test
    public void testNestedMap() {
        String expectedSchema = """
{
  "type": "object",
  "properties": {
    "attribute": {
      "type": "string",
      "description": "Top level attribute"
    },
    "map": {
      "type": "object",
      "description": "Map subattribute",
      "additionalProperties": {
        "type": "string"
      }
    }
  },
  "required": ["attribute"]
}
""";
        assertTrue(check(NestedMap.class, expectedSchema), "generated schema does not match expected schema");
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

    private static boolean check(Class<?> clazz, String expectedSchema) {
    	var jsonSchema = schema(clazz);

        var schema = new OpenAiSchema(jsonSchema);

        var openAiSchema = schema.toJsonNode();
         
    	if (!compare(openAiSchema, expectedSchema)) {
            System.out.println("JacksonSchema: " + jsonSchema);
    		System.out.println("Expected Schema:" + expectedSchema);
            System.out.println("Generated Schema: " + openAiSchema.toPrettyString());
            return false;
    	}
    	return true;
    }

    private static boolean compare(JsonNode nodeSchema, String expectedSchema) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode nodeSchema2 = objectMapper.readTree(expectedSchema);

            return nodeSchema.equals(nodeSchema2);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
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
    @JsonPropertyDescription("Map subattribute") @JsonProperty(required = false) Map<String, String> map) {}
