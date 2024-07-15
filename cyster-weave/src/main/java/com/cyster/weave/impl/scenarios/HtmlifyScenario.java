package com.cyster.weave.impl.scenarios;

import org.springframework.stereotype.Component;

import com.cyster.ai.weave.service.conversation.Conversation;
import com.cyster.ai.weave.service.scenario.Scenario;

@Component
public class HtmlifyScenario implements Scenario<Void, Void> {
    private static final String NAME = "htmlify";

    private ChatScenario chatScenario;

    HtmlifyScenario(ChatScenario chatScenario) {
        this.chatScenario = chatScenario;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Marks up text with html";
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
        throw new UnsupportedOperationException("Method is deprectated and being removed from interface");
    }

    @Override
    public ConversationBuilder createConversationBuilder(Void parameters, Void context) {
        String instructions = """
Convert the input to a marked up HTML fragment.

Everything should be included in <p></p> tags. If there are lists use <ul> or <ol> tags.

Just returned the marked up fragment, nothing else.
""";

        return this.chatScenario.createConversationBuilder(parameters, context)
            .setOverrideInstructions(instructions);
    }  
}
