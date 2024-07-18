package com.extole.weave.scenarios.runbooks;

import java.io.StringReader;
import java.io.StringWriter;

import org.springframework.stereotype.Component;

import com.extole.weave.scenarios.support.ExtoleSupportHelpScenario;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Component
public class ExtoleRunbookDefault implements RunbookScenario {
    public static String NAME = "extoleRunbookOther";
    private static String DESCRIPTION = "Analyzes and comments on tickets that could not be classfied more specifically";
    private static String KEYWORDS = "nothing";

    private static String INSTRUCTIONS = """
Load the support ticket {{ticket_number}}

Note the ticket number, and note its classified as "other".
""";

    private ExtoleSupportHelpScenario helpScenario;

    ExtoleRunbookDefault(ExtoleSupportHelpScenario helpScenario) {
        this.helpScenario = helpScenario;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getKeywords() {
        return KEYWORDS;
    }

    @Override
    public Class<RunbookScenarioParameters> getParameterClass() {
        return RunbookScenarioParameters.class;
    }

    @Override
    public Class<Void> getContextClass() {
        return Void.class;
    }

    @Override
    public ConversationBuilder createConversationBuilder(RunbookScenarioParameters parameters, Void context) {
        MustacheFactory mostacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mostacheFactory.compile(new StringReader(INSTRUCTIONS), "instructions");
        var messageWriter = new StringWriter();
        mustache.execute(messageWriter, parameters);
        messageWriter.flush();
        
        var instructions = messageWriter.toString();

        return this.helpScenario.createConversationBuilder(null, null).setOverrideInstructions(instructions);
    }
}

