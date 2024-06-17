package com.cyster.app.sage.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.cyster.ai.weave.service.conversation.Operation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageResponse {
    private final String type;
    private final String content;
    private final OperationResponse operations;
    
    public MessageResponse(String type, String content, Operation operations) {
        this.type = type;
        this.content = content;
        this.operations = createOperationResponse(operations);
    }
    
    public String getType() {
        return this.type;
    }
    
    public String getContent() {
        return this.content;
    }
    
    public OperationResponse getOperations() {
        return operations;
    }
    
    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static OperationResponse createOperationResponse(Operation operation) {
        if (operation.children().size() == 0) {
            return new OperationResponseNoChildren(operation);
        } 
        else {
            return new OperationResponseWithChildren(operation);
        }
    }
    
    public static interface OperationResponse {
        String getDescription();
        Object getContext(); 
    }
    
    public static class OperationResponseNoChildren implements OperationResponse {
        String description;
        Optional<Object> context;
        
        public OperationResponseNoChildren(Operation operation) {
           this.description = operation.getDescription();
           this.context = operation.context();
        }

        public String getDescription() {
            return this.description;
        }

        public Object getContext() {
            return this.context;
        }     
    }

    public static class OperationResponseWithChildren implements OperationResponse {
        String description;
        List<OperationResponse> children;
        Optional<Object> context;
        
        public OperationResponseWithChildren(Operation operation) {
           this.description = operation.getDescription();
           this.children = new ArrayList<>();
           for (Operation child: operation.children()) {
               children.add(createOperationResponse(child));
           }
           this.context = operation.context();
        }

        public String getDescription() {
            return this.description;
        }

        public List<OperationResponse> getChildren() {
            return this.children;
        }

        public Object getContext() {
            return this.context;
        }     
    }
}
