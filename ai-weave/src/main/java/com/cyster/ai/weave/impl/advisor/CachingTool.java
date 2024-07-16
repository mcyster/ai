package com.cyster.ai.weave.impl.advisor;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.cyster.ai.weave.service.Tool;
import com.cyster.ai.weave.service.ToolException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CachingTool<Request, Context> implements Tool<Request, Context> {
    private final Tool<Request, Context> tool;
    private final Cache<Key<Request, Context>, Object> cache;

    private CachingTool(Tool<Request, Context> tool, Cache<Key<Request, Context>, Object> cache) {
        this.tool = tool;
        this.cache = cache;
    }

    public static <Request, Context> CachingTool.Builder<Request, Context> builder(Tool<Request, Context> tool) {
        return new CachingTool.Builder<Request, Context>(tool);
    }

    @Override
    public String getName() {
        return this.tool.getName();
    }

    @Override
    public String getDescription() {
        return this.tool.getDescription();
    }

    @Override
    public Class<Request> getParameterClass() {
        return this.tool.getParameterClass();
    }

    @Override
    public Object execute(Request request, Context context) throws ToolException {
        Key<Request, Context> key = new Key<Request, Context>(this.tool, request, context);

        try {
            return cache.get(key, () -> execute(key));
        } catch(ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof ToolException) {
                throw (ToolException)cause;
            }
            else {
               throw new RuntimeException("Unexpected exception while executing tool: " + this.tool.getName(), exception);
            }
        }
    }

    private static <Request, Context> Object execute(Key<Request, Context> key) throws ToolException {
        return key.getTool().execute(key.getRequest(), key.getContext());
    }

    private static class Key<Request, Context> {
        private Request request;
        private Context context;
        private Tool<Request, Context> tool;

        Key(Tool<Request, Context> tool, Request request, Context context) {
            this.tool = tool;
            this.request = request;
            this.context = context;
        }

        @JsonProperty
        Tool<Request, Context> getTool() {
            return this.tool;
        }

        @JsonProperty
        Request getRequest() {
            return this.request;
        }

        @JsonProperty
        Context getContext() {
            return this.context;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }

            Key<?, ?> key = (Key<?, ?>) object;
            return Objects.equals(request, key.request) &&
                   Objects.equals(context, key.context);
        }

        @Override
        public int hashCode() {
            return Objects.hash(request, context);
        }

        @Override
        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException("Error converting object of class " + this.getClass().getName() + " JSON", exception);
            }
        }
    }

    public static class Builder<Request, Context> {
        private final Tool<Request, Context> tool;
        private int size = 1000;
        private long duration = 1;
        private TimeUnit durationUnit = TimeUnit.HOURS;

        private Builder(Tool<Request, Context> tool) {
            this.tool = tool;
        }

        public CachingTool<Request, Context> build() {
            Cache<Key<Request, Context>, Object> cache = CacheBuilder.newBuilder()
                .maximumSize(size)
                .expireAfterWrite(this.duration, this.durationUnit)
                .build();

            return new CachingTool<Request, Context>(this.tool, cache);
        }

    }
}
