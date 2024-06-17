package com.cyster.ai.weave.service.conversation;

public interface Message {

    public enum Type {
        SYSTEM("System"), AI("Ai"), USER("User"), ERROR("Error"), INFO("Info"), FUNCTION_CALL("Function Call"),
        FUNCTION_RESULT("Function Result");

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
    
    Operation operation();   
       
}
