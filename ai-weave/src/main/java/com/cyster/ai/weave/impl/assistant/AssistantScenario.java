package com.cyster.ai.weave.impl.assistant;

import java.util.List;

import com.cyster.ai.weave.impl.advisor.Advisor;
import com.cyster.ai.weave.impl.advisor.assistant.OperationImpl;
import com.cyster.ai.weave.impl.advisor.assistant.WeaveOperation;
import com.cyster.ai.weave.service.Weave;
import com.cyster.ai.weave.service.conversation.ActiveConversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.cyster.ai.weave.service.conversation.ScenarioConversation;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.ai.weave.service.scenario.ScenarioType;

public class AssistantScenario<PARAMETERS, CONTEXT> implements Scenario<PARAMETERS, CONTEXT> {
    private Advisor<CONTEXT> advisor;
    private String description;
    private Class<PARAMETERS> parameterClass;
    private Class<CONTEXT> contextClass;

    public AssistantScenario(Advisor<CONTEXT> advisor, String description, Class<PARAMETERS> parameterClass,
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
    public ConversationBuilder createConversationBuilder(PARAMETERS parameters, CONTEXT context) {
        WeaveOperation logger = new OperationImpl(WeaveOperation.Level.Normal, advisor.getName(), context);

        return new ConversationBuilderImpl(this, parameters, context, logger,
                this.advisor.createConversation().withContext(context));
    }

    private static class ConversationBuilderImpl implements ConversationBuilder {
        Advisor.AdvisorConversationBuilder<?> advisorConversationBuilder;
        private final Scenario<?, ?> scenario;
        private Object parameters;
        private Object context;
        private WeaveOperation logger;

        ConversationBuilderImpl(Scenario<?, ?> scenario, Object parameters, Object context, WeaveOperation logger,
                Advisor.AdvisorConversationBuilder<?> advisorConversationBuilder) {
            this.scenario = scenario;
            this.parameters = parameters;
            this.context = context;
            this.logger = logger;
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

        public ScenarioConversation start() {
            return new AssistantScenarioConversation(scenario.toScenarioType(), parameters, context, logger,
                    this.advisorConversationBuilder.start());
        }

    }

    public static class AssistantScenarioConversation implements ScenarioConversation, Weave {
        private final ScenarioType scenarioType;
        private final Object parameters;
        private final Object context;
        private final WeaveOperation operation;
        private final ActiveConversation advisorConversation;

        public AssistantScenarioConversation(ScenarioType scenarioType, Object parameters, Object context,
                WeaveOperation operation, ActiveConversation advisorConversation) {
            this.scenarioType = scenarioType;
            this.parameters = parameters;
            this.context = context;
            this.operation = operation;
            this.advisorConversation = advisorConversation;
        }

        @Override
        public String id() {
            return advisorConversation.id();
        }

        @Override
        public ScenarioType scenarioType() {
            return scenarioType;
        }

        @Override
        public Object parameters() {
            return parameters;
        }

        @Override
        public Object context() {
            return context;
        }

        @Override
        public Message addMessage(Type type, String message) {
            return advisorConversation.addMessage(type, message);
        }

        @Override
        public Message respond() throws ConversationException {
            return advisorConversation.respond();
        }

        @Override
        public Message respond(Weave weave) throws ConversationException {
            return advisorConversation.respond(weave);
        }

        @Override
        public List<Message> messages() {
            return advisorConversation.messages();
        }

        @Override
        public ActiveConversation conversation() {
            return this;
        }

        @Override
        public WeaveOperation operation() {
            return operation;
        }

    }
}
