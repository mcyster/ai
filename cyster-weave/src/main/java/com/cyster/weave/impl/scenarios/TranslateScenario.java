package com.cyster.weave.impl.scenarios;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.impl.advisor.MessageImpl;
import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.weave.impl.advisors.SimpleAdvisor;
import com.cyster.weave.impl.scenarios.TranslateScenario.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class TranslateScenario implements Scenario<Parameters, Void> {
    private static final String NAME = "translate";
    private Advisor<Void> advisor;


    TranslateScenario(SimpleAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Translates a message from one language to another";
    }
    
    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }
    
    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Parameters parameters, Void context) {
        return new Builder(this.advisor).setParameters(parameters).start();
    }
    
    private static class LocalizeConversation implements Conversation {
        private Conversation conversation;

        LocalizeConversation(Conversation conversation) {
            this.conversation = conversation;
        }

        public Message addMessage(Type type, String content) {
            return this.conversation.addMessage(type, content);
        }

        @Override
        public Message respond() throws ConversationException {
            List<Message> messages = this.conversation.getMessages();
            if (messages.size() == 0 || messages.get(messages.size() - 1).getType() != Message.Type.USER) {
                throw new ConversationException("This conversation scenaio requires a user prompt");
            }
            return this.conversation.respond();
        }

        @Override
        public List<Message> getMessages() {
            return this.conversation.getMessages();
        }

    }

    public class Builder {
        private Advisor<Void> advisor;
        private Parameters parameters;
        
        Builder(Advisor<Void> advisor) {
            this.advisor = advisor;
        }
        
        public Builder setParameters(Parameters parameters) {
            this.parameters = parameters;
            return this;
        }

        public Conversation start() {
            String systemPrompt = "Please translate messages from {{language}} to {{target_language}}.";

            MustacheFactory mostacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mostacheFactory.compile(new StringReader(systemPrompt), "system_prompt");
            var messageWriter = new StringWriter();
            mustache.execute(messageWriter, this.parameters);
            messageWriter.flush();
            var instructions = messageWriter.toString();
            
            Conversation conversation  = this.advisor.createConversation()
                .setOverrideInstructions(instructions)
                .start();
            
            return new LocalizeConversation(conversation);
        }
    }

    static class Parameters {
        @JsonPropertyDescription("language to translate from, ISO 639-1, defaults to en")
        @JsonProperty(required = false)
        public String language;

        @JsonPropertyDescription("the language to which to translate the text, ISO 639-1")
        @JsonProperty(required = true)
        public String target_language;
    }
}
