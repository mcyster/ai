package com.cyster.sage.impl.scenarios;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.advisor.Advisor;
import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.conversation.ConversationException;
import com.cyster.ai.weave.service.conversation.Message;
import com.cyster.ai.weave.service.conversation.Message.Type;
import com.cyster.ai.weave.service.scenario.Scenario;
import com.cyster.sage.impl.advisors.SimpleAdvisor;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class ChatScenario implements Scenario<Void, Void> {
    private static final String NAME = "chat";
    private Advisor<Void> advisor;


    ChatScenario(SimpleAdvisor advisor) {
        this.advisor = advisor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Chat with the AI";
    }
    
    @Override
    public Class<Void> getParameterClass() {
        return Void.class;
    }
    
    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public Conversation createConversation(Void parameters, Void context) {
        return new Builder(this.advisor).start();
    }
    
    private static class LocalizeConversation implements Conversation {
        private Conversation conversation;

        LocalizeConversation(Conversation conversation) {
            this.conversation = conversation;
        }

        @Override
        public Message addMessage(Type type, String message) {
            return this.conversation.addMessage(type, message);
        }

        @Override
        public Message respond() throws ConversationException {
            return this.conversation.respond();
        }

        @Override
        public List<Message> getMessages() {
            return this.conversation.getMessages();
        }


    }

    public class Builder {
        private Advisor<Void> advisor;
        private Void parameters;
        
        Builder(Advisor<Void> advisor) {
            this.advisor = advisor;
        }
        
        public Builder setParameters(Void parameters) {
            this.parameters = parameters;
            return this;
        }

        public Conversation start() {
            String systemPrompt = "Enjoy a chat, say hi if there is no prompt.";

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

}
