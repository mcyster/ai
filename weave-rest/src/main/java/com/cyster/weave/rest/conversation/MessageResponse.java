package com.cyster.weave.rest.conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.cyster.ai.weave.service.conversation.Operation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface MessageResponse {
    enum Level {
        debug,
        verbose,
        normal,
        quiet;
    }

    String getType();
    String getContent();


    public static class QuietMessageResponse implements MessageResponse {
        private final String type;
        private final String content;

        private QuietMessageResponse(String type, String content) {
            this.type = type;
            this.content = content;
        }

        @Override
        public String getType() {
            return this.type;
        }

        @Override
        public String getContent() {
            return this.content;
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
    }

    public static class VerboseMessageResponse implements MessageResponse {
        private final OperationResponse operation;
        private final String type;
        private final String content;


        private VerboseMessageResponse(String type, String content, Operation operation) {
            this.type = type;
            this.content = content;
            this.operation = createOperationResponse(operation);
        }

        @Override
        public String getType() {
            return this.type;
        }

        @Override
        public String getContent() {
            return this.content;
        }

        public OperationResponse getOperation() {
            return operation;
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
        Level getLevel();
        String getDescription();
        Object getContext();
    }

    public static class OperationResponseNoChildren implements OperationResponse {
        MessageResponse.Level level;
        String description;
        Optional<Object> context;

        public OperationResponseNoChildren(Operation operation) {
            this.level = toResponseLevel(operation.getLevel());
            this.description = operation.getDescription();
            this.context = operation.context();
        }

        @Override
        public Level getLevel() {
            return this.level;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public Object getContext() {
            return this.context;
        }
    }

    public static class OperationResponseWithChildren implements OperationResponse {
        MessageResponse.Level level;
        String description;
        List<OperationResponse> children;
        Optional<Object> context;

        public OperationResponseWithChildren(Operation operation) {
            this.level = toResponseLevel(operation.getLevel());
            this.description = operation.getDescription();
            this.children = new ArrayList<>();
            for (Operation child: operation.children()) {
                children.add(createOperationResponse(child));
            }
            this.context = operation.context();
        }

        @Override
        public Level getLevel() {
            return level;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        public List<OperationResponse> getChildren() {
            return this.children;
        }

        @Override
        public Object getContext() {
            return this.context;
        }
    }

    public static MessageResponse.Level toResponseLevel(Operation.Level level) {
        return switch (level) {
            case Normal -> MessageResponse.Level.normal;
            case Verbose -> MessageResponse.Level.verbose;
            case Debug -> MessageResponse.Level.debug;
        };
    }

    public static Operation.Level toLevel(MessageResponse.Level level) {
        return switch (level) {
            case quiet -> Operation.Level.Normal;
            case normal -> Operation.Level.Normal;
            case verbose -> Operation.Level.Verbose;
            case debug -> Operation.Level.Debug;
        };
    }

    public static class Builder {
        MessageResponse.Level responseLevel;

        public Builder(MessageResponse.Level level) {
            this.responseLevel = level;
        }

        public MessageResponse create(String type, String content, Operation operation) {
            if (responseLevel == MessageResponse.Level.quiet) {
                return new QuietMessageResponse(type, content);
            } else {
                return new VerboseMessageResponse(type, content, new FilteredOperation(operation, toLevel(this.responseLevel)));
            }
        }
    }

    public static class FilteredOperation implements Operation {
        private final Operation original;
        private final List<Operation> filteredChildren;

        public FilteredOperation(Operation original, Operation.Level level) {
            this.original = original;
            this.filteredChildren = filterOperations(original.children(), level);
        }

        @Override
        public Level getLevel() {
            return original.getLevel();
        }

        @Override
        public String getDescription() {
            return original.getDescription();
        }

        @Override
        public List<Operation> children() {
            return filteredChildren;
        }

        @Override
        public Optional<Object> context() {
            return original.context();
        }
    }

    private static List<Operation> filterOperations(List<Operation> operations, Operation.Level level) {
        return operations.stream()
            .filter(operation -> operation.getLevel().ordinal() >= level.ordinal())
            .map(operation -> new FilteredOperation(operation, level))
            .collect(Collectors.toList());
    }
}
