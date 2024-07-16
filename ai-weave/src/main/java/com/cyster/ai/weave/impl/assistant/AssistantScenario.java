package com.cyster.ai.weave.impl.assistant;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;

public class AssistantScenario<PARAMETERS, CONTEXT> implements Scenario<PARAMETERS, CONTEXT>{
    private Advisor<CONTEXT> advisor;
    private String description;
    private Class<PARAMETERS> parameterClass; 
    private Class<CONTEXT> contextClass;
    
    public AssistantScenario(
        Advisor<CONTEXT> advisor,
        String description,
        Class<PARAMETERS> parameterClass,
        Class<CONTEXT> contextClass) {
        this.advisor = advisor;
        this.description = description;
        this.parameterClass = parameterClass;
        this.contextClass = contextClass;
    }
    
    @Override
    public String getName() {
        return this.advisor.getName();
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Class<PARAMETERS> getParameterClass() {
        return this.parameterClass;
    }

    @Override
    public Class<CONTEXT> getContextClass() {
        return this.contextClass;
    }

    @Override
    public Conversation createConversation(PARAMETERS parameters, CONTEXT context) {
        return createConversationBuilder(parameters, context).start();
    }

    @Override
    public ConversationBuilder createConversationBuilder(PARAMETERS parameters, CONTEXT context) {
        return new ConversationBuilderImpl(this.advisor.createConversation());
    }
    
    private static class ConversationBuilderImpl implements ConversationBuilder {
        Advisor.AdvisorConversationBuilder<?> advisorConversationBuilder;
        
        ConversationBuilderImpl(Advisor.AdvisorConversationBuilder<?> advisorConversationBuilder) {
            this.advisorConversationBuilder = advisorConversationBuilder;
        }

        @Override
        public ConversationBuilder setOverrideInstructions(String instructions) {
            this.advisorConversationBuilder.setOverrideInstructions(instructions);
            return this;
        }
        
        public ConversationBuilder addMessage(String message) {
            this.advisorConversationBuilder.addMessage(message);
            return this;            
        }
        
        public Conversation start() {
            return this.advisorConversationBuilder.start();
        }


    }

}
