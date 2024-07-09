package com.cyster.weave.rest.scenario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;

public class ScenarioResponse {

    private String name;
    private String description;
    private Class<?> parameterClass;

    public ScenarioResponse(String name, String description, Class<?> parameterClass) {
        this.name = name;
        this.description = description;
        this.parameterClass = parameterClass;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }
    
    public JsonSchema getParameters() {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(mapper);
        
        try {
            return schemaGenerator.generateSchema(parameterClass);
        } catch (JsonMappingException exception) {
            throw new RuntimeException(exception);
        }
    }

    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static class Builder {
        private String name;
        private String description = "";
        private Class<?> parameterClass = Void.class;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }
        
        public Builder setParameterClass(Class<?> parameterClass) {
            this.parameterClass = parameterClass;
            return this;
        }

        public ScenarioResponse build() {
            return new ScenarioResponse(this.name, this.description, this.parameterClass);
        }
    }

}
