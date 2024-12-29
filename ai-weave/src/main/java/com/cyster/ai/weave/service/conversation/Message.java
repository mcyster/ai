package com.cyster.ai.weave.service.conversation;

import com.cyster.ai.weave.impl.WeaveOperation;

public interface Message {

    public enum Type {
        SYSTEM("System"), AI("Ai"), USER("User"), ERROR("Error");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    Type getType();

    String getContent();

    WeaveOperation operation();

}
